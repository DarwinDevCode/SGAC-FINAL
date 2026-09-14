CREATE OR REPLACE FUNCTION postulacion.fn_registrar_puntaje_jurado(
    p_id_evaluacion_oposicion INTEGER,
    p_id_usuario              INTEGER,
    p_puntaje_material        NUMERIC(5,2),
    p_puntaje_exposicion      NUMERIC(5,2),
    p_puntaje_respuestas      NUMERIC(5,2),
    p_finalizar               BOOLEAN DEFAULT FALSE
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_codigo_estado     TEXT;
    v_finalizo          BOOLEAN;
    v_id_comision       INTEGER;
    v_todos_finalizaron BOOLEAN;
    v_puntaje_total     NUMERIC(5,2);
BEGIN
    -- Estado actual de la evaluación
    SELECT tee.codigo
    INTO   v_codigo_estado
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.tipo_estado_evaluacion tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Evaluación no encontrada.');
    END IF;

    IF v_codigo_estado != 'EN_CURSO' THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Solo se puede calificar cuando la evaluación está EN CURSO. Estado actual: ' || v_codigo_estado
               );
    END IF;

    -- Verificar que el usuario sea miembro activo de la comisión correspondiente
    SELECT uc.id_comision_seleccion, uc.finalizo_calificacion
    INTO   v_id_comision, v_finalizo
    FROM   seguridad.usuario_comision      uc
               JOIN   postulacion.comision_seleccion  cs ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   postulacion.postulacion          p  ON p.id_convocatoria       = cs.id_convocatoria
               JOIN   postulacion.evaluacion_oposicion eo ON eo.id_postulacion       = p.id_postulacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
      AND  uc.id_usuario              = p_id_usuario
      AND  uc.activo                  = TRUE
      AND  cs.activo                  = TRUE
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El usuario no pertenece al tribunal de esta convocatoria.'
               );
    END IF;

    IF v_finalizo THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Ya finalizaste tu calificación. No puedes modificarla.'
               );
    END IF;

    -- Validar topes antes de llegar a los CHECK de la BD
    IF p_puntaje_material  > 10.00 THEN RETURN jsonb_build_object('exito', false, 'mensaje', 'Material: el puntaje máximo es 10.00'); END IF;
    IF p_puntaje_exposicion >  4.00 THEN RETURN jsonb_build_object('exito', false, 'mensaje', 'Exposición: el puntaje máximo es 4.00'); END IF;
    IF p_puntaje_respuestas >  6.00 THEN RETURN jsonb_build_object('exito', false, 'mensaje', 'Respuestas: el puntaje máximo es 6.00'); END IF;
    IF p_puntaje_material < 0 OR p_puntaje_exposicion < 0 OR p_puntaje_respuestas < 0 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Los puntajes no pueden ser negativos.');
    END IF;

    -- Guardar notas del jurado
    UPDATE seguridad.usuario_comision
    SET    puntaje_material        = p_puntaje_material,
           puntaje_exposicion      = p_puntaje_exposicion,
           puntaje_respuestas      = p_puntaje_respuestas,
           id_evaluacion_oposicion = p_id_evaluacion_oposicion,
           fecha_evaluacion        = CURRENT_DATE,
           finalizo_calificacion   = p_finalizar
    WHERE  id_usuario            = p_id_usuario
      AND  id_comision_seleccion = v_id_comision
      AND  activo                = TRUE;

    IF p_finalizar THEN
        -- Verificar si TODOS los miembros activos ya finalizaron
        SELECT BOOL_AND(uc.finalizo_calificacion) INTO v_todos_finalizaron
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion = v_id_comision
          AND  uc.activo = TRUE;

        IF v_todos_finalizaron THEN
            -- ── FIX CRÍTICO: dividir SIEMPRE para 3.0 (total oficial de jurados)
            -- SUM() sobre COALESCE garantiza que los nulls cuenten como 0,
            -- manteniendo la ponderación correcta aunque alguien no haya calificado.
            SELECT ROUND(
                           COALESCE(SUM(
                                            COALESCE(uc.puntaje_material,   0) +
                                            COALESCE(uc.puntaje_exposicion, 0) +
                                            COALESCE(uc.puntaje_respuestas, 0)
                                    ), 0) / 3.0
                       , 2)
            INTO   v_puntaje_total
            FROM   seguridad.usuario_comision uc
            WHERE  uc.id_comision_seleccion = v_id_comision
              AND  uc.activo = TRUE;

            UPDATE postulacion.evaluacion_oposicion
            SET    puntaje_total_oposicion   = v_puntaje_total,
                   id_tipo_estado_evaluacion = (
                       SELECT id_tipo_estado_evaluacion
                       FROM   postulacion.tipo_estado_evaluacion
                       WHERE  codigo = 'FINALIZADA'
                   )
            WHERE  id_evaluacion_oposicion = p_id_evaluacion_oposicion;

            RETURN jsonb_build_object(
                    'exito',            true,
                    'mensaje',          'Puntaje guardado. Todos los jurados finalizaron. Nota final calculada.',
                    'todosFinalizaron', true,
                    'puntajeFinal',     v_puntaje_total
                   );
        END IF;
    END IF;

    RETURN jsonb_build_object(
            'exito',            true,
            'mensaje',          CASE WHEN p_finalizar THEN 'Calificación finalizada y bloqueada.'
                                     ELSE 'Puntaje guardado correctamente.' END,
            'todosFinalizaron', false,
            'subtotal',         p_puntaje_material + p_puntaje_exposicion + p_puntaje_respuestas
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ─────────────────────────────────────────────────────────────────────
-- FIX 2: fn_cambiar_estado_evaluacion — promedio siempre / 3.0
-- ─────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION postulacion.fn_cambiar_estado_evaluacion(
    p_id_evaluacion_oposicion INTEGER,
    p_accion                  TEXT
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_codigo_actual   TEXT;
    v_id_estado_nuevo INTEGER;
    v_puntaje_total   NUMERIC(5,2);
    v_id_comision     INTEGER;
BEGIN
    p_accion := UPPER(TRIM(COALESCE(p_accion, '')));

    SELECT tee.codigo
    INTO   v_codigo_actual
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.tipo_estado_evaluacion tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Evaluación no encontrada.');
    END IF;

    IF p_accion = 'INICIAR' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede iniciar una evaluación PROGRAMADA. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'EN_CURSO';

        UPDATE postulacion.evaluacion_oposicion
        SET    id_tipo_estado_evaluacion = v_id_estado_nuevo,
               hora_inicio_real          = CURRENT_TIME
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',    true,
                'mensaje',  'Evaluación iniciada. El tribunal puede comenzar a calificar.',
                'horaReal', to_char(LOCALTIME, 'HH24:MI:SS')
               );
    END IF;

    IF p_accion = 'NO_PRESENTO' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede marcar como No Presentó desde estado PROGRAMADA.'
                   );
        END IF;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'NO_PRESENTO';

        UPDATE postulacion.evaluacion_oposicion
        SET    id_tipo_estado_evaluacion = v_id_estado_nuevo
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object('exito', true, 'mensaje', 'Postulante marcado como No Presentó.');
    END IF;

    IF p_accion = 'FINALIZAR' THEN
        IF v_codigo_actual != 'EN_CURSO' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede finalizar una evaluación EN CURSO. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        SELECT cs.id_comision_seleccion INTO v_id_comision
        FROM   postulacion.evaluacion_oposicion eo
                   JOIN   postulacion.postulacion          p  ON p.id_postulacion   = eo.id_postulacion
                   JOIN   postulacion.comision_seleccion   cs ON cs.id_convocatoria = p.id_convocatoria
        WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  cs.activo = TRUE
        LIMIT 1;

        -- ── FIX CRÍTICO: SUM() / 3.0 para mantener la ponderación oficial
        -- Aunque un jurado no haya calificado, la fórmula divide para 3.
        SELECT ROUND(
                       COALESCE(SUM(
                                        COALESCE(uc.puntaje_material,   0) +
                                        COALESCE(uc.puntaje_exposicion, 0) +
                                        COALESCE(uc.puntaje_respuestas, 0)
                                ), 0) / 3.0
                   , 2)
        INTO   v_puntaje_total
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion    = v_id_comision
          AND  uc.id_evaluacion_oposicion  = p_id_evaluacion_oposicion
          AND  uc.activo                   = TRUE;

        UPDATE seguridad.usuario_comision
        SET    finalizo_calificacion = TRUE
        WHERE  id_comision_seleccion   = v_id_comision
          AND  id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  activo = TRUE;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'FINALIZADA';

        UPDATE postulacion.evaluacion_oposicion
        SET    puntaje_total_oposicion   = COALESCE(v_puntaje_total, 0),
               hora_fin_real             = CURRENT_TIME,
               id_tipo_estado_evaluacion = v_id_estado_nuevo
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',        true,
                'mensaje',      'Evaluación finalizada y bloqueada.',
                'puntajeFinal', COALESCE(v_puntaje_total, 0),
                'horaFin',      to_char(LOCALTIME, 'HH24:MI:SS')
               );
    END IF;

    RETURN jsonb_build_object('exito', false, 'mensaje', 'Acción no reconocida: ' || p_accion);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ─────────────────────────────────────────────────────────────────────
-- NUEVA FUNCIÓN: fn_obtener_mi_turno(p_id_convocatoria, p_id_usuario)
--
-- Vista filtrada exclusiva para el postulante autenticado.
-- El backend extrae p_id_usuario del JWT y nunca confía en el frontend.
-- Retorna únicamente el turno del estudiante con su desglose de jurados,
-- sin exponer los datos de los otros postulantes.
-- ─────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION postulacion.fn_obtener_mi_turno(
    p_id_convocatoria INTEGER,
    p_id_usuario      INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_turno JSONB;
BEGIN
    SELECT jsonb_build_object(
                   'idEvaluacionOposicion', eo.id_evaluacion_oposicion,
                   'orden',                 eo.orden_exposicion,
                   'nombres',               u.nombres,
                   'apellidos',             u.apellidos,
                   'correo',                u.correo,
                   'tema',                  eo.tema_exposicion,
                   'fecha',                 to_char(eo.fecha_evaluacion, 'YYYY-MM-DD'),
                   'horaInicio',            to_char(eo.hora_inicio, 'HH24:MI'),
                   'horaFin',               to_char(eo.hora_fin, 'HH24:MI'),
                   'horaInicioReal',        to_char(eo.hora_inicio_real, 'HH24:MI'),
                   'horaFinReal',           to_char(eo.hora_fin_real, 'HH24:MI'),
                   'lugar',                 eo.lugar,
                   'estado',                tee.codigo,
                   'nombreEstado',          tee.nombre,
                   'puntajeFinal',          eo.puntaje_total_oposicion,
               -- Desglose de notas de cada jurado (visible al postulante solo si FINALIZADA)
                   'jurados', (
                       SELECT COALESCE(
                                      jsonb_agg(jsonb_build_object(
                                                        'idUsuario',           uj.id_usuario,
                                                        'nombres',             uj2.nombres,
                                                        'apellidos',           uj2.apellidos,
                                                        'rol',                 uj.rol_integrante,
                                                    -- Solo revelar notas individuales si la evaluación ya terminó
                                                        'puntajeMaterial',  CASE WHEN tee.codigo = 'FINALIZADA'
                                                                                     THEN uj.puntaje_material   ELSE NULL END,
                                                        'puntajeExposicion',CASE WHEN tee.codigo = 'FINALIZADA'
                                                                                     THEN uj.puntaje_exposicion ELSE NULL END,
                                                        'puntajeRespuestas',CASE WHEN tee.codigo = 'FINALIZADA'
                                                                                     THEN uj.puntaje_respuestas ELSE NULL END,
                                                        'subtotal', CASE WHEN tee.codigo = 'FINALIZADA'
                                                                             THEN COALESCE(uj.puntaje_material,   0) +
                                                                                  COALESCE(uj.puntaje_exposicion, 0) +
                                                                                  COALESCE(uj.puntaje_respuestas, 0)
                                                                         ELSE 0 END,
                                                        'finalizo', uj.finalizo_calificacion
                                                ) ORDER BY uj.rol_integrante),
                                      '[]'::jsonb
                              )
                       FROM   seguridad.usuario_comision uj
                                  JOIN   seguridad.usuario          uj2 ON uj2.id_usuario = uj.id_usuario
                                  JOIN   postulacion.comision_seleccion cs
                                         ON cs.id_comision_seleccion = uj.id_comision_seleccion
                                             AND cs.id_convocatoria = p_id_convocatoria
                                             AND cs.activo = TRUE
                   )
           )
    INTO v_turno
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.postulacion             p   ON p.id_postulacion   = eo.id_postulacion
               JOIN   academico.estudiante                est ON est.id_estudiante  = p.id_estudiante
               JOIN   seguridad.usuario                   u   ON u.id_usuario       = est.id_usuario
               JOIN   postulacion.tipo_estado_evaluacion  tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  p.id_convocatoria = p_id_convocatoria
      AND  est.id_usuario    = p_id_usuario
      AND  p.activo          = TRUE;

    IF v_turno IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No se encontró un turno de oposición asignado para tu postulación en esta convocatoria.'
               );
    END IF;

    RETURN jsonb_build_object('exito', true, 'turno', v_turno);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;