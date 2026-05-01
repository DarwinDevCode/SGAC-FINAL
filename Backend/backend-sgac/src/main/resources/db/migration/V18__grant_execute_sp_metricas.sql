-- V18: Garantizar permisos de ejecución en funciones del schema public
-- y asegurar que los SPs de métricas existen correctamente.

-- Permiso de ejecución en todas las funciones del schema public
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO app_user_default;

-- Recrear SP de métricas coordinador (por si V15 no aplicó correctamente)
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
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera)::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN convocatoria.tipo_estado_convocatoria tec ON cv.id_tipo_estado_convocatoria = tec.id_tipo_estado_convocatoria
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(tec.nombre_estado_convocatoria) IN ('ABIERTA','ACTIVA','PUBLICADA'))::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera)::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(tep.nombre_estado_postulacion) = 'PENDIENTE')::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(tep.nombre_estado_postulacion) = 'APROBADO')::BIGINT;
END;
$$ LANGUAGE plpgsql;

-- Recrear SP de métricas postulante (por si V15 no aplicó correctamente)
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
        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         WHERE e.id_usuario = p_id_usuario)::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(tep.nombre_estado_postulacion) = 'PENDIENTE')::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(tep.nombre_estado_postulacion) = 'APROBADO')::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(tep.nombre_estado_postulacion) = 'RECHAZADO')::BIGINT;
END;
$$ LANGUAGE plpgsql;

-- Permisos de ejecución explícitos en los SPs de métricas
GRANT EXECUTE ON FUNCTION public.sp_metricas_convocatoria(INTEGER) TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_metricas_postulante(INTEGER)   TO app_user_default;
