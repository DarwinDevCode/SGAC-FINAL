CREATE OR REPLACE FUNCTION postulacion.fn_generar_comisiones_automaticas()
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_conv              RECORD;
    v_id_comision       INTEGER;
    v_id_usr_decano     INTEGER;
    v_id_usr_coord      INTEGER;
    v_id_usr_docente    INTEGER;
    v_contador          INTEGER := 0;
    v_omitidas          INTEGER := 0;
    v_nombre_comision   TEXT;
BEGIN
    FOR v_conv IN
        SELECT
            c.id_convocatoria,
            c.id_periodo_academico,
            a.nombre_asignatura,
            a.id_carrera,
            ca.id_facultad,
            d.id_usuario AS id_usuario_docente
        FROM  convocatoria.convocatoria   c
                  JOIN  academico.asignatura        a  ON a.id_asignatura = c.id_asignatura
                  JOIN  academico.carrera           ca ON ca.id_carrera   = a.id_carrera
                  JOIN  academico.docente           d  ON d.id_docente    = c.id_docente
        WHERE c.activo = TRUE

          AND EXISTS (
            SELECT 1 FROM postulacion.postulacion p
            WHERE  p.id_convocatoria = c.id_convocatoria
              AND  p.activo = TRUE
        )
          AND NOT EXISTS (
            SELECT 1 FROM postulacion.comision_seleccion cs
            WHERE  cs.id_convocatoria = c.id_convocatoria
              AND  cs.activo = TRUE
        )
          AND EXISTS (
            SELECT 1
            FROM  postulacion.postulacion          p
                      JOIN  postulacion.tipo_estado_postulacion tep
                            ON tep.id_tipo_estado_postulacion = p.id_tipo_estado_postulacion
            WHERE p.id_convocatoria = c.id_convocatoria
              AND p.activo          = TRUE
              AND tep.codigo        = 'APROBADA'
        )
        ORDER BY c.id_convocatoria
        LOOP

            SELECT de.id_usuario INTO v_id_usr_decano
            FROM   academico.decano de
            WHERE  de.id_facultad = v_conv.id_facultad
              AND  de.activo = TRUE
            ORDER  BY de.id_decano DESC
            LIMIT  1;

            IF v_id_usr_decano IS NULL THEN
                RAISE WARNING '[COMISION] Sin decano activo — facultad=%, convocatoria=%',
                    v_conv.id_facultad, v_conv.id_convocatoria;
                v_omitidas := v_omitidas + 1;
                CONTINUE;
            END IF;

            SELECT co.id_usuario INTO v_id_usr_coord
            FROM   academico.coordinador co
            WHERE  co.id_carrera = v_conv.id_carrera
              AND  co.activo = TRUE
            ORDER  BY co.id_coordinador DESC
            LIMIT  1;

            IF v_id_usr_coord IS NULL THEN
                RAISE WARNING '[COMISION] Sin coordinador activo — carrera=%, convocatoria=%',
                    v_conv.id_carrera, v_conv.id_convocatoria;
                v_omitidas := v_omitidas + 1;
                CONTINUE;
            END IF;

            v_id_usr_docente := v_conv.id_usuario_docente;

            v_nombre_comision :=
                    'Comisión · ' || v_conv.nombre_asignatura
                        || ' · ' || to_char(CURRENT_DATE, 'DD/MM/YYYY');


            INSERT INTO postulacion.comision_seleccion (
                id_convocatoria, nombre_comision, fecha_conformacion, activo
            ) VALUES (
                         v_conv.id_convocatoria,
                         v_nombre_comision,
                         CURRENT_DATE,
                         TRUE
                     )
            RETURNING id_comision_seleccion INTO v_id_comision;

            INSERT INTO seguridad.usuario_comision
            (id_comision_seleccion, id_usuario, rol_integrante, activo)
            VALUES
                (v_id_comision, v_id_usr_decano,  'DECANO',      TRUE),
                (v_id_comision, v_id_usr_coord,   'COORDINADOR', TRUE),
                (v_id_comision, v_id_usr_docente, 'DOCENTE',     TRUE);

            v_contador := v_contador + 1;
        END LOOP;

    RETURN jsonb_build_object(
            'exito',             true,
            'mensaje',           'Proceso completado. Creadas: ' || v_contador
                                     || '. Omitidas (miembro faltante): ' || v_omitidas || '.',
            'comisiones_creadas', v_contador,
            'convocatorias_omitidas', v_omitidas
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito',              false,
            'mensaje',            '[ERROR] ' || SQLERRM,
            'comisiones_creadas', 0,
            'convocatorias_omitidas', 0
           );
END;
$$;







-- ────────────────────────────────────────────────────────────────────
-- 2. fn_consultar_comision_detalle(p_id_usuario, p_rol)
--
--    Vista ESTUDIANTE:
--      Devuelve los miembros de la comisión asignada a su postulación.
--
--    Vista DECANO | COORDINADOR | DOCENTE:
--      Lista de convocatorias donde está asignado como evaluador,
--      con postulantes y fechas de la fase de Evaluación de Méritos
--      y Oposición obtenidas de periodo_fase.
--
--    p_rol aceptado: 'ESTUDIANTE' | 'DECANO' | 'COORDINADOR' | 'DOCENTE'
-- ────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION postulacion.fn_consultar_comision_detalle(
    p_id_usuario INTEGER,
    p_rol        TEXT
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    p_rol := UPPER(TRIM(COALESCE(p_rol, '')));

    -- ════════════════════════════════════════════════════════
    -- VISTA ESTUDIANTE
    -- ════════════════════════════════════════════════════════
    IF p_rol = 'ESTUDIANTE' THEN

        SELECT jsonb_build_object(
                       'rol',       'ESTUDIANTE',
                       'comisiones', COALESCE(jsonb_agg(bloque ORDER BY bloque->>'nombreComision'), '[]'::jsonb)
               )
        INTO v_resultado
        FROM (
                 SELECT jsonb_build_object(
                                'idComision',         cs.id_comision_seleccion,
                                'idConvocatoria',     c.id_convocatoria,
                                'nombreAsignatura',   a.nombre_asignatura,
                                'nombreComision',     cs.nombre_comision,
                                'fechaConformacion',  to_char(cs.fecha_conformacion, 'YYYY-MM-DD'),
                                'miembros', (
                                    SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                      'idUsuario',  u2.id_usuario,
                                                                      'nombres',    u2.nombres,
                                                                      'apellidos',  u2.apellidos,
                                                                      'cargo',      uc.rol_integrante,
                                                                      'correo',     u2.correo
                                                              ) ORDER BY uc.rol_integrante), '[]'::jsonb)
                                    FROM  seguridad.usuario_comision uc
                                              JOIN  seguridad.usuario           u2 ON u2.id_usuario = uc.id_usuario
                                    WHERE uc.id_comision_seleccion = cs.id_comision_seleccion
                                )
                        ) AS bloque
                 FROM  academico.estudiante           e
                           JOIN  postulacion.postulacion         po ON po.id_estudiante   = e.id_estudiante
                           JOIN  postulacion.comision_seleccion  cs ON cs.id_convocatoria = po.id_convocatoria
                           JOIN  convocatoria.convocatoria        c  ON c.id_convocatoria  = cs.id_convocatoria
                           JOIN  academico.asignatura             a  ON a.id_asignatura   = c.id_asignatura
                 WHERE e.id_usuario = p_id_usuario
                   AND po.activo    = TRUE
                   AND cs.activo    = TRUE
             ) sub;

        IF v_resultado IS NULL OR v_resultado->>'comisiones' = '[]' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'No se encontró comisión asignada para este estudiante.',
                    'rol',     'ESTUDIANTE'
                   );
        END IF;

        RETURN jsonb_build_object('exito', true) || v_resultado;
    END IF;

    -- ════════════════════════════════════════════════════════
    -- VISTA MIEMBRO DEL COMITÉ (DECANO / COORDINADOR / DOCENTE)
    -- ════════════════════════════════════════════════════════
    IF p_rol IN ('DECANO', 'COORDINADOR', 'DOCENTE') THEN

        SELECT jsonb_build_object(
                       'rol',           p_rol,
                       'convocatorias', COALESCE(jsonb_agg(bloque ORDER BY bloque->>'nombreAsignatura'), '[]'::jsonb)
               )
        INTO v_resultado
        FROM (
                 SELECT jsonb_build_object(
                                'idConvocatoria',   c.id_convocatoria,
                                'nombreAsignatura', a.nombre_asignatura,
                                'idComision',       cs.id_comision_seleccion,
                                'nombreComision',   cs.nombre_comision,
                                'fechaConformacion', to_char(cs.fecha_conformacion, 'YYYY-MM-DD'),
                            -- Fechas de la fase "Evaluación de Méritos y Oposición"
                                'faseEvaluacion', (
                                    SELECT jsonb_build_object(
                                                   'nombreFase', tf.nombre,
                                                   'codigoFase', tf.codigo,
                                                   'fechaInicio', to_char(pf.fecha_inicio, 'YYYY-MM-DD'),
                                                   'fechaFin',    to_char(pf.fecha_fin,    'YYYY-MM-DD')
                                           )
                                    FROM  planificacion.periodo_fase pf
                                              JOIN  planificacion.tipo_fase    tf ON tf.id_tipo_fase = pf.id_tipo_fase
                                    WHERE pf.id_periodo_academico = c.id_periodo_academico
                                      AND (tf.codigo = 'EVALUACION_MER_OPO' OR tf.nombre ILIKE '%meritos%' OR tf.nombre ILIKE '%oposici%')
                                    ORDER BY tf.orden ASC
                                    LIMIT 1
                                ),
                            -- Lista de postulantes de esa convocatoria
                                'postulantes', (
                                    SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                      'idPostulacion',    po.id_postulacion,
                                                                      'nombres',          u2.nombres,
                                                                      'apellidos',        u2.apellidos,
                                                                      'correo',           u2.correo,
                                                                      'fechaPostulacion', to_char(po.fecha_postulacion, 'YYYY-MM-DD'),
                                                                      'estadoPostulacion', COALESCE(tep.nombre, 'PENDIENTE'),
                                                                      'codigoEstado',      COALESCE(tep.codigo, 'PENDIENTE')
                                                              ) ORDER BY u2.apellidos, u2.nombres), '[]'::jsonb)
                                    FROM  postulacion.postulacion              po
                                              JOIN  academico.estudiante                 est ON est.id_estudiante = po.id_estudiante
                                              JOIN  seguridad.usuario                    u2  ON u2.id_usuario     = est.id_usuario
                                              LEFT  JOIN postulacion.tipo_estado_postulacion tep
                                                         ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
                                    WHERE po.id_convocatoria = c.id_convocatoria
                                      AND po.activo = TRUE
                                )
                        ) AS bloque
                 FROM  seguridad.usuario_comision     uc
                           JOIN  postulacion.comision_seleccion cs ON cs.id_comision_seleccion = uc.id_comision_seleccion
                           JOIN  convocatoria.convocatoria       c  ON c.id_convocatoria        = cs.id_convocatoria
                           JOIN  academico.asignatura            a  ON a.id_asignatura          = c.id_asignatura
                 WHERE uc.id_usuario     = p_id_usuario
                   AND uc.rol_integrante = p_rol
                   AND cs.activo         = TRUE
             ) sub;

        IF v_resultado IS NULL OR v_resultado->>'convocatorias' = '[]' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'No se encontraron comisiones asignadas para este usuario con rol ' || p_rol || '.',
                    'rol',     p_rol
                   );
        END IF;

        RETURN jsonb_build_object('exito', true) || v_resultado;
    END IF;

    -- Rol no reconocido
    RETURN jsonb_build_object(
            'exito',   false,
            'mensaje', 'Rol no reconocido. Valores aceptados: ESTUDIANTE, DECANO, COORDINADOR, DOCENTE.',
            'rol',     p_rol
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito',   false,
            'mensaje', '[ERROR] ' || SQLERRM
           );
END;
$$;