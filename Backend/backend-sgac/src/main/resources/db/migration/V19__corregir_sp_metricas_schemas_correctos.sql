-- V19: Corregir SP sp_metricas_convocatoria con schemas y columnas reales
-- La postulacion está en postulacion.postulacion (NO convocatoria)
-- El estado se guarda directo como texto en columna 'estado' y 'estado_postulacion'
-- No existen tablas tipo_estado_convocatoria ni tipo_estado_postulacion

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
        -- Total convocatorias de la carrera
        (SELECT COUNT(*)
         FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera
           AND cv.activo = TRUE)::BIGINT,

        -- Convocatorias activas (estado ABIERTA, ACTIVA o PUBLICADA)
        (SELECT COUNT(*)
         FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera
           AND cv.activo = TRUE
           AND UPPER(cv.estado) IN ('ABIERTA','ACTIVA','PUBLICADA'))::BIGINT,

        -- Total postulaciones de la carrera
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera)::BIGINT,

        -- Postulaciones pendientes
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(p.estado_postulacion) = 'PENDIENTE')::BIGINT,

        -- Postulaciones aprobadas
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(p.estado_postulacion) = 'APROBADO')::BIGINT;
END;
$$ LANGUAGE plpgsql;

-- SP postulante corregido
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
         WHERE e.id_usuario = p_id_usuario)::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'PENDIENTE')::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'APROBADO')::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'RECHAZADO')::BIGINT;
END;
$$ LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION public.sp_metricas_convocatoria(INTEGER) TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_metricas_postulante(INTEGER)   TO app_user_default;
