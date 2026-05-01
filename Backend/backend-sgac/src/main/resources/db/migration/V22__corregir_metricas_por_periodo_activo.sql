-- V22: Add active academic period filter to KPI stored procedures

CREATE OR REPLACE FUNCTION public.sp_metricas_convocatoria(p_id_carrera INTEGER)
RETURNS TABLE(
    total_convocatorias        BIGINT,
    convocatorias_activas      BIGINT,
    total_postulaciones        BIGINT,
    postulaciones_pendientes   BIGINT,
    postulaciones_aprobadas    BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        -- Total convocatorias de la carrera en periodo activo
        (SELECT COUNT(*)
         FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE a.id_carrera = p_id_carrera
           AND cv.activo = TRUE
           AND pa.activo = TRUE)::BIGINT,

        -- Convocatorias activas (estado ABIERTA, ACTIVA o PUBLICADA) en periodo activo
        (SELECT COUNT(*)
         FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE a.id_carrera = p_id_carrera
           AND cv.activo = TRUE
           AND UPPER(cv.estado) IN ('ABIERTA','ACTIVA','PUBLICADA')
           AND pa.activo = TRUE)::BIGINT,

        -- Total postulaciones de la carrera en periodo activo
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE a.id_carrera = p_id_carrera
           AND pa.activo = TRUE)::BIGINT,

        -- Postulaciones pendientes en periodo activo
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(p.estado_postulacion) = 'PENDIENTE'
           AND pa.activo = TRUE)::BIGINT,

        -- Postulaciones aprobadas en periodo activo
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(p.estado_postulacion) = 'APROBADO'
           AND pa.activo = TRUE)::BIGINT;
END;
$$ LANGUAGE plpgsql;

-- SP postulante corregido con periodo activo
CREATE OR REPLACE FUNCTION public.sp_metricas_postulante(p_id_usuario INTEGER)
RETURNS TABLE(
    total_postulaciones        BIGINT,
    postulaciones_pendientes   BIGINT,
    postulaciones_aprobadas    BIGINT,
    postulaciones_rechazadas   BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND pa.activo = TRUE)::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'PENDIENTE'
           AND pa.activo = TRUE)::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'APROBADO'
           AND pa.activo = TRUE)::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'RECHAZADO'
           AND pa.activo = TRUE)::BIGINT;
END;
$$ LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION public.sp_metricas_convocatoria(INTEGER) TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_metricas_postulante(INTEGER)   TO app_user_default;
