CREATE OR REPLACE FUNCTION postulacion.fn_listar_convocatorias_para_oposicion()
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   academico.periodo_academico
        WHERE  activo  = TRUE
          AND  estado  = 'EN PROCESO'
    ) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en estado EN PROCESO.'
               );
    END IF;

    SELECT COALESCE(
                   jsonb_agg(
                           jsonb_build_object(
                                   'idConvocatoria',       c.id_convocatoria,
                                   'nombreAsignatura',     a.nombre_asignatura,
                                   'semestreAsignatura',   a.semestre,
                                   'nombreCarrera',        ca.nombre_carrera,
                                   'nombreFacultad',       f.nombre_facultad,
                                   'nombreDocente',        u.nombres || ' ' || u.apellidos,
                                   'cuposDisponibles',     c.cupos_disponibles,
                                   'estadoConvocatoria',   c.estado,
                                   'totalPostulantesAptos', (
                                       SELECT COUNT(*)
                                       FROM   postulacion.postulacion          po
                                                  JOIN   postulacion.tipo_estado_postulacion tep
                                                         ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
                                       WHERE  po.id_convocatoria = c.id_convocatoria
                                         AND  po.activo          = TRUE
                                         AND  UPPER(tep.codigo)  = 'APROBADA'
                                   ),
                               -- Estado del proceso de oposición para esta convocatoria
                                   'tieneComision', EXISTS (
                               SELECT 1
                               FROM   postulacion.comision_seleccion cs
                               WHERE  cs.id_convocatoria = c.id_convocatoria
                                 AND  cs.activo          = TRUE
                           ),
                                   'tieneSorteo', EXISTS (
                               SELECT 1
                               FROM   postulacion.evaluacion_oposicion eo
                                          JOIN   postulacion.postulacion            po
                                                 ON po.id_postulacion = eo.id_postulacion
                               WHERE  po.id_convocatoria = c.id_convocatoria
                           )
                           ) ORDER BY a.nombre_asignatura ASC
                   ),
                   '[]'::jsonb
           )
    INTO v_resultado
    FROM  convocatoria.convocatoria       c
              JOIN  academico.asignatura        a   ON a.id_asignatura   = c.id_asignatura
              JOIN  academico.carrera           ca  ON ca.id_carrera     = a.id_carrera
              JOIN  academico.facultad          f   ON f.id_facultad     = ca.id_facultad
              JOIN  academico.docente           d   ON d.id_docente      = c.id_docente
              JOIN  seguridad.usuario           u   ON u.id_usuario      = d.id_usuario
              JOIN  academico.periodo_academico p   ON p.id_periodo_academico = c.id_periodo_academico
    WHERE  c.activo      = TRUE
      AND  p.activo      = TRUE
      AND  p.estado      = 'EN PROCESO'
      AND  UPPER(c.estado) IN ('ABIERTA', 'EN_EVALUACION')
      AND  EXISTS (
        SELECT 1
        FROM   postulacion.postulacion              po2
                   JOIN   postulacion.tipo_estado_postulacion  tep2
                          ON tep2.id_tipo_estado_postulacion = po2.id_tipo_estado_postulacion
        WHERE  po2.id_convocatoria = c.id_convocatoria
          AND  po2.activo          = TRUE
          AND  UPPER(tep2.nombre)  = 'APROBADA'
    );

    -- ── Respuesta ─────────────────────────────────────────────
    IF jsonb_array_length(v_resultado) = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No hay convocatorias con postulantes aprobados en el período activo.'
               );
    END IF;

    RETURN jsonb_build_object(
            'exito', true,
            'datos', v_resultado
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito',   false,
            'mensaje', '[ERROR] ' || SQLERRM
           );
END;
$$;

-- Permisos
GRANT EXECUTE ON FUNCTION postulacion.fn_listar_convocatorias_para_oposicion()
    TO app_user_default;