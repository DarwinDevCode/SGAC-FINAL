CREATE OR REPLACE FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador(p_id_usuario integer, p_id_postulacion integer)
    RETURNS jsonb
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_resultado JSONB;
    v_postulacion_carrera INTEGER;
BEGIN
    -- 1. Validar coordinador y su carrera
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RAISE EXCEPTION 'AVISO: El usuario no tiene rol de coordinador activo asignado';
    END IF;

    -- 2. Validar existencia y pertenencia de la postulación
    SELECT a.id_carrera INTO v_postulacion_carrera
    FROM postulacion.postulacion p
             JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
             JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RAISE EXCEPTION 'AVISO: La postulación no existe';
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RAISE EXCEPTION 'AVISO: No tiene permisos para ver esta postulación (pertenece a otra carrera)';
    END IF;

    -- 3. Construir respuesta JSON consolidada
    WITH fechas_convocatoria AS (
        -- Extraemos las fechas del calendario centralizado para esta convocatoria
        SELECT
            pf.id_periodo_academico,
            MIN(CASE WHEN tf.codigo = 'PUBLICACION_OFERTA' THEN pf.fecha_inicio END) as fecha_pub,
            MAX(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_fin END) as fecha_cie
        FROM planificacion.periodo_fase pf
                 JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
        GROUP BY pf.id_periodo_academico
    ),
         conteo_docs AS (
             -- Resumen optimizado de documentos en una sola pasada
             SELECT
                 ra.id_postulacion,
                 COUNT(*) as total,
                 COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'PENDIENTE') as pendientes,
                 COUNT(*) FILTER (WHERE UPPER(ter.codigo) IN ('APROBADO', 'VALIDADO')) as aprobados,
                 COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'OBSERVADO') as observados,
                 COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'RECHAZADO') as rechazados,
                 COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'CORREGIDO') as corregidos
             FROM postulacion.requisito_adjunto ra
                      JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
             WHERE ra.id_postulacion = p_id_postulacion
             GROUP BY ra.id_postulacion
         )
    SELECT jsonb_build_object(
                   'postulacion', jsonb_build_object(
                    'id_postulacion', p.id_postulacion,
                    'fecha_postulacion', p.fecha_postulacion,
                    'estado_nombre', tep.codigo,
                    'observaciones', COALESCE(p.observaciones, '')
                                  ),
                   'estudiante', jsonb_build_object(
                           'id_estudiante', e.id_estudiante,
                           'nombre_completo', u.nombres || ' ' || u.apellidos,
                           'email', u.correo,
                           'matricula', e.matricula,
                           'semestre', e.semestre
                                 ),
                   'convocatoria', jsonb_build_object(
                           'id_convocatoria', cv.id_convocatoria,
                           'asignatura', a.nombre_asignatura,
                           'docente', ud.nombres || ' ' || ud.apellidos,
                           'fecha_publicacion', fc.fecha_pub,
                           'fecha_cierre', fc.fecha_cie,
                           'cupos_disponibles', cv.cupos_disponibles
                                   ),
                   'documentos', (
                       SELECT COALESCE(jsonb_agg(
                                               jsonb_build_object(
                                                       'id_requisito_adjunto', ra.id_requisito_adjunto,
                                                       'tipo_requisito', trp.nombre_requisito,
                                                       'nombre_archivo', ra.nombre_archivo,
                                                       'estado', ter.codigo,
                                                       'observacion', ra.observacion,
                                                       'tiene_archivo', (ra.archivo IS NOT NULL)
                                               )
                                       ), '[]'::jsonb)
                       FROM postulacion.requisito_adjunto ra
                                JOIN convocatoria.tipo_requisito_postulacion trp ON ra.id_tipo_requisito_postulacion = trp.id_tipo_requisito_postulacion
                                JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                       WHERE ra.id_postulacion = p.id_postulacion
                   ),
                   'resumen_documentos', jsonb_build_object(
                           'total', COALESCE(cd.total, 0),
                           'pendientes', COALESCE(cd.pendientes, 0),
                           'aprobados', COALESCE(cd.aprobados, 0),
                           'observados', COALESCE(cd.observados, 0),
                           'rechazados', COALESCE(cd.rechazados, 0),
                           'corregidos', COALESCE(cd.corregidos, 0)
                                         ),
                   'puede_aprobar', COALESCE(cd.total > 0 AND cd.total = cd.aprobados, FALSE)
           ) INTO v_resultado
    FROM postulacion.postulacion p
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
             JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
             JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
             JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
             JOIN academico.docente d ON cv.id_docente = d.id_docente
             JOIN seguridad.usuario ud ON d.id_usuario = ud.id_usuario
             JOIN fechas_convocatoria fc ON cv.id_periodo_academico = fc.id_periodo_academico
             LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
             LEFT JOIN conteo_docs cd ON p.id_postulacion = cd.id_postulacion
    WHERE p.id_postulacion = p_id_postulacion;

    RETURN v_resultado;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'ERROR SISTEMA [%]: %', SQLSTATE, SQLERRM;
END;
$$;