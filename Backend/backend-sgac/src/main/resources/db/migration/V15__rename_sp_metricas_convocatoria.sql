-- V15: Renombrar/recrear SPs de dashboard a metricas_convocatoria
-- Se usa CREATE OR REPLACE para garantizar que existan con el nombre nuevo,
-- independientemente de si el SP anterior existe o no en la BD.

-- SP KPIs coordinador → sp_metricas_convocatoria
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

-- SP KPIs postulante → sp_metricas_postulante
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

-- Eliminar los SPs anteriores si aún existen con el nombre viejo
DROP FUNCTION IF EXISTS public.sp_dashboard_coordinador(integer);
DROP FUNCTION IF EXISTS public.sp_dashboard_postulante(integer);
