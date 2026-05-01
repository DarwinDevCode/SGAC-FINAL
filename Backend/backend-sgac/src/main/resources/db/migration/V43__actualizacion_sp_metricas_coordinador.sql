CREATE OR REPLACE FUNCTION public.sp_metricas_convocatoria(p_id_carrera integer)
    RETURNS TABLE(
                     total_convocatorias bigint,
                     convocatorias_activas bigint,
                     total_postulaciones bigint,
                     postulaciones_pendientes bigint,
                     postulaciones_aprobadas bigint
                 )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            (SELECT COUNT(*)
             FROM convocatoria.convocatoria cv
                      JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND cv.activo = TRUE
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM convocatoria.convocatoria cv
                      JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND cv.activo = TRUE
               AND UPPER(cv.estado) IN ('ABIERTA','ACTIVA','PUBLICADA')
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM postulacion.postulacion p
                      JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
                      JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM postulacion.postulacion p
                      JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
                      JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
                      JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND UPPER(tep.codigo) = 'PENDIENTE'
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM postulacion.postulacion p
                      JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
                      JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
                      JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND UPPER(tep.codigo) = 'APROBADO'
               AND pa.activo = TRUE)::BIGINT;
END;
$$;