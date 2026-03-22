-- V75__fix_coordinador_permissions.sql
-- Fixes permission denied errors for the Coordinator role

-- 1. Grant USAGE on schemas to role_coordinador
GRANT USAGE ON SCHEMA seguridad TO role_coordinador;
GRANT USAGE ON SCHEMA academico TO role_coordinador;
GRANT USAGE ON SCHEMA postulacion TO role_coordinador;
GRANT USAGE ON SCHEMA convocatoria TO role_coordinador;

-- 2. Grant SELECT on specific tables to role_coordinador
GRANT SELECT ON seguridad.usuario TO role_coordinador;
GRANT SELECT ON ALL TABLES IN SCHEMA academico TO role_coordinador;
GRANT SELECT ON ALL TABLES IN SCHEMA postulacion TO role_coordinador;
GRANT SELECT ON ALL TABLES IN SCHEMA convocatoria TO role_coordinador;

-- 3. Ensure administrador_consultas has full SELECT permissions as function owner
GRANT USAGE ON SCHEMA seguridad TO administrador_consultas;
GRANT USAGE ON SCHEMA academico TO administrador_consultas;
GRANT USAGE ON SCHEMA postulacion TO administrador_consultas;
GRANT USAGE ON SCHEMA convocatoria TO administrador_consultas;

GRANT SELECT ON ALL TABLES IN SCHEMA seguridad TO administrador_consultas;
GRANT SELECT ON ALL TABLES IN SCHEMA academico TO administrador_consultas;
GRANT SELECT ON ALL TABLES IN SCHEMA postulacion TO administrador_consultas;
GRANT SELECT ON ALL TABLES IN SCHEMA convocatoria TO administrador_consultas;

-- 4. Redefine functions as SECURITY DEFINER and set owner to administrador_consultas

-- fn_listar_postulaciones_coordinador
ALTER FUNCTION postulacion.fn_listar_postulaciones_coordinador(integer) SECURITY DEFINER;
ALTER FUNCTION postulacion.fn_listar_postulaciones_coordinador(integer) OWNER TO administrador_consultas;

-- fn_obtener_detalle_postulacion_coordinador
ALTER FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador(integer, integer) SECURITY DEFINER;
ALTER FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador(integer, integer) OWNER TO administrador_consultas;

-- fn_evaluar_documento_individual
ALTER FUNCTION postulacion.fn_evaluar_documento_individual(integer, integer, varchar, text) SECURITY DEFINER;
ALTER FUNCTION postulacion.fn_evaluar_documento_individual(integer, integer, varchar, text) OWNER TO administrador_consultas;

-- fn_dictaminar_postulacion
ALTER FUNCTION postulacion.fn_dictaminar_postulacion(integer, integer, varchar, text) SECURITY DEFINER;
ALTER FUNCTION postulacion.fn_dictaminar_postulacion(integer, integer, varchar, text) OWNER TO administrador_consultas;

-- fn_cambiar_estado_postulacion_revision
ALTER FUNCTION postulacion.fn_cambiar_estado_postulacion_revision(integer, integer) SECURITY DEFINER;
ALTER FUNCTION postulacion.fn_cambiar_estado_postulacion_revision(integer, integer) OWNER TO administrador_consultas;

-- fn_consultar_cronograma_oposicion (Verify owner and perms)
ALTER FUNCTION postulacion.fn_consultar_cronograma_oposicion(integer) SECURITY DEFINER;
ALTER FUNCTION postulacion.fn_consultar_cronograma_oposicion(integer) OWNER TO administrador_consultas;

-- 5. Grant EXECUTE to role_coordinador
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA postulacion TO role_coordinador;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA academico TO role_coordinador;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA convocatoria TO role_coordinador;

-- 6. Define missing functions for Coordinator Dashboard and Reports

-- fn_obtener_estadisticas_coordinador
CREATE OR REPLACE FUNCTION academico.fn_obtener_estadisticas_coordinador(p_id_usuario INTEGER)
RETURNS TABLE (
    total_convocatorias_propias BIGINT,
    convocatorias_activas BIGINT,
    convocatorias_inactivas BIGINT,
    total_postulantes_recibidos BIGINT,
    postulantes_aprobados BIGINT,
    postulantes_rechazados BIGINT,
    postulantes_en_evaluacion BIGINT,
    postulantes_pendientes BIGINT,
    top_convocatorias JSON
) AS $$
DECLARE
    v_id_carrera INTEGER;
BEGIN
    SELECT id_carrera INTO v_id_carrera FROM academico.coordinador WHERE id_usuario = p_id_usuario AND activo = TRUE;

    RETURN QUERY
    SELECT
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv 
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura 
         WHERE a.id_carrera = v_id_carrera AND cv.activo = TRUE)::BIGINT,
        
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv 
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura 
         WHERE a.id_carrera = v_id_carrera AND cv.activo = TRUE AND cv.estado IN ('ABIERTA', 'PUBLICADA'))::BIGINT,
        
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv 
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura 
         WHERE a.id_carrera = v_id_carrera AND cv.activo = TRUE AND cv.estado NOT IN ('ABIERTA', 'PUBLICADA'))::BIGINT,
        
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         WHERE a.id_carrera = v_id_carrera AND p.activo = TRUE)::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = v_id_carrera AND p.activo = TRUE AND tep.codigo = 'APROBADA')::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = v_id_carrera AND p.activo = TRUE AND tep.codigo = 'RECHAZADA')::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = v_id_carrera AND p.activo = TRUE AND tep.codigo IN ('EN_EVALUACION', 'EN_REVISION'))::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = v_id_carrera AND p.activo = TRUE AND tep.codigo = 'PENDIENTE')::BIGINT,
         
        (SELECT json_agg(t) FROM (
            SELECT a.nombre_asignatura AS "tituloConvocatoria", COUNT(p.id_postulacion) AS "cantidadPostulantes"
            FROM convocatoria.convocatoria cv
            JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
            LEFT JOIN postulacion.postulacion p ON cv.id_convocatoria = p.id_convocatoria AND p.activo = TRUE
            WHERE a.id_carrera = v_id_carrera AND cv.activo = TRUE
            GROUP BY a.nombre_asignatura
            ORDER BY "cantidadPostulantes" DESC
            LIMIT 5
        ) t);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
ALTER FUNCTION academico.fn_obtener_estadisticas_coordinador(INTEGER) OWNER TO administrador_consultas;

-- fn_reporte_convocatorias_coordinador
CREATE OR REPLACE FUNCTION academico.fn_reporte_convocatorias_coordinador(p_id_usuario INTEGER)
RETURNS TABLE (
    id_convocatoria INTEGER,
    nombre_asignatura VARCHAR,
    nombre_carrera VARCHAR,
    nombre_periodo VARCHAR,
    fecha_inicio DATE,
    fecha_fin DATE,
    cupos_aprobados INTEGER,
    estado VARCHAR,
    numero_postulantes BIGINT
) AS $$
DECLARE
    v_id_carrera INTEGER;
BEGIN
    SELECT id_carrera INTO v_id_carrera FROM academico.coordinador WHERE id_usuario = p_id_usuario AND activo = TRUE;

    RETURN QUERY
    WITH fechas AS (
        SELECT
            pf.id_periodo_academico,
            MIN(pf.fecha_inicio) FILTER (WHERE tf.codigo = 'PUBLICACION_OFERTA') as f_inicio,
            MAX(pf.fecha_fin) FILTER (WHERE tf.codigo = 'POSTULACION') as f_fin
        FROM planificacion.periodo_fase pf
        JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
        GROUP BY pf.id_periodo_academico
    )
    SELECT 
        cv.id_convocatoria,
        a.nombre_asignatura::VARCHAR,
        car.nombre_carrera::VARCHAR,
        pa.nombre_periodo::VARCHAR,
        f.f_inicio AS fecha_inicio,
        f.f_fin AS fecha_fin,
        cv.cupos_disponibles AS cupos_aprobados,
        cv.estado::VARCHAR,
        (SELECT COUNT(*) FROM postulacion.postulacion p WHERE p.id_convocatoria = cv.id_convocatoria AND p.activo = TRUE)::BIGINT
    FROM convocatoria.convocatoria cv
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
    LEFT JOIN fechas f ON pa.id_periodo_academico = f.id_periodo_academico
    WHERE car.id_carrera = v_id_carrera AND cv.activo = TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
ALTER FUNCTION academico.fn_reporte_convocatorias_coordinador(INTEGER) OWNER TO administrador_consultas;

-- fn_reporte_postulantes_coordinador
CREATE OR REPLACE FUNCTION academico.fn_reporte_postulantes_coordinador(p_id_usuario INTEGER)
RETURNS TABLE (
    id_postulacion INTEGER,
    nombre_estudiante VARCHAR,
    cedula VARCHAR,
    nombre_asignatura VARCHAR,
    nombre_periodo VARCHAR,
    fecha_postulacion DATE,
    estado_evaluacion VARCHAR
) AS $$
DECLARE
    v_id_carrera INTEGER;
BEGIN
    SELECT id_carrera INTO v_id_carrera FROM academico.coordinador WHERE id_usuario = p_id_usuario AND activo = TRUE;

    RETURN QUERY
    SELECT 
        p.id_postulacion,
        (u.nombres || ' ' || u.apellidos)::VARCHAR AS nombre_estudiante,
        u.cedula::VARCHAR,
        a.nombre_asignatura::VARCHAR,
        pa.nombre_periodo::VARCHAR,
        p.fecha_postulacion,
        tep.nombre::VARCHAR AS estado_evaluacion
    FROM postulacion.postulacion p
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
    JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE a.id_carrera = v_id_carrera AND p.activo = TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
ALTER FUNCTION academico.fn_reporte_postulantes_coordinador(INTEGER) OWNER TO administrador_consultas;

-- Final grants
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA academico TO role_coordinador;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA postulacion TO role_coordinador;
