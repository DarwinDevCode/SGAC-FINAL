DROP FUNCTION IF EXISTS postulacion.fn_ver_detalle_postulacion(INTEGER);

CREATE OR REPLACE FUNCTION postulacion.fn_ver_detalle_postulacion(p_id_usuario integer)
    RETURNS jsonb
    SECURITY DEFINER
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id_estudiante INTEGER;
    v_resultado JSONB;
BEGIN
    SELECT u.id_rol_especifico INTO v_id_estudiante
    FROM seguridad.fn_identidad_usuario(p_id_usuario) u
    WHERE u.nombre_rol = 'ESTUDIANTE'
    LIMIT 1;

    IF v_id_estudiante IS NULL THEN
        RAISE EXCEPTION 'Acceso denegado: El usuario no es un estudiante activo.';
    END IF;

    WITH info_postulacion AS (
        SELECT
            p.id_postulacion,
            p.fecha_postulacion,
            tep.nombre AS nombre_estado_postulacion,
            p.observaciones,
            c.id_convocatoria,
            c.cupos_disponibles,
            c.estado AS estado_convocatoria_admin,
            c.id_periodo_academico,
            a.nombre_asignatura,
            a.semestre AS semestre_asignatura,
            ca.nombre_carrera,
            (u_doc.nombres || ' ' || u_doc.apellidos) AS nombre_docente
        FROM postulacion.postulacion p
                 INNER JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
                 INNER JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
                 INNER JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
                 INNER JOIN academico.carrera ca ON a.id_carrera = ca.id_carrera
                 INNER JOIN academico.docente d ON c.id_docente = d.id_docente
                 INNER JOIN seguridad.usuario u_doc ON d.id_usuario = u_doc.id_usuario
        WHERE p.id_estudiante = v_id_estudiante
          AND p.activo = TRUE
        ORDER BY p.fecha_postulacion DESC
        LIMIT 1
    ),
         cronograma_data AS (
             SELECT
                 pf.id_periodo_academico,
                 jsonb_agg(jsonb_build_object(
                                   'fase', tf.nombre,
                                   'codigo', tf.codigo,
                                   'inicio', pf.fecha_inicio,
                                   'fin', pf.fecha_fin,
                                   'estado', CASE
                                                 WHEN CURRENT_DATE < pf.fecha_inicio THEN 'PENDIENTE'
                                                 WHEN CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin THEN 'EN CURSO'
                                                 ELSE 'FINALIZADA'
                                       END
                           ) ORDER BY tf.orden) as etapas
             FROM planificacion.periodo_fase pf
                      JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
             WHERE pf.id_periodo_academico = (SELECT id_periodo_academico FROM info_postulacion)
             GROUP BY pf.id_periodo_academico
         ),
         conteo_documentos AS (
             SELECT
                 jsonb_build_object(
                         'pendientes', COUNT(*) FILTER (WHERE ter.nombre_estado = 'PENDIENTE'),
                         'aprobados',   COUNT(*) FILTER (WHERE ter.nombre_estado = 'APROBADO'),
                         'observados',  COUNT(*) FILTER (WHERE ter.nombre_estado = 'OBSERVADO'),
                         'rechazados',  COUNT(*) FILTER (WHERE ter.nombre_estado = 'RECHAZADO'),
                         'corregidos',  COUNT(*) FILTER (WHERE ter.nombre_estado = 'CORREGIDO')
                 ) as resumen
             FROM postulacion.requisito_adjunto ra
                      INNER JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
             WHERE ra.id_postulacion = (SELECT id_postulacion FROM info_postulacion)
         )
    SELECT jsonb_build_object(
                   'exito', TRUE,
                   'mensaje', 'Detalle de postulación recuperado',
                   'postulacion', jsonb_build_object(
                           'id_postulacion', ip.id_postulacion,
                           'fecha_postulacion', ip.fecha_postulacion,
                           'estado_nombre', ip.nombre_estado_postulacion,
                           'observaciones', COALESCE(ip.observaciones, '')
                                  ),
                   'convocatoria', jsonb_build_object(
                           'id_convocatoria', ip.id_convocatoria,
                           'nombre_asignatura', ip.nombre_asignatura,
                           'semestre_asignatura', ip.semestre_asignatura,
                           'nombre_carrera', ip.nombre_carrera,
                           'nombre_docente', ip.nombre_docente,
                           'cupos_disponibles', ip.cupos_disponibles,
                           'estado_admin', ip.estado_convocatoria_admin
                                   ),
                   'cronograma', cd.etapas,
                   'resumen_documentos', doc.resumen
           ) INTO v_resultado
    FROM info_postulacion ip
             CROSS JOIN cronograma_data cd
             CROSS JOIN conteo_documentos doc;

    IF v_resultado IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'SIN_POSTULACION',
                'mensaje', 'No tienes ninguna postulación activa'
               );
    END IF;

    RETURN v_resultado;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'ERROR SISTEMA [%]: %', SQLSTATE, SQLERRM;
END;
$$;