-- 1. Crear funciones faltantes para EvaluacionOposicion
DROP FUNCTION IF EXISTS public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS public.sp_actualizar_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS public.sp_listar_evaluaciones_oposicion();

CREATE OR REPLACE FUNCTION public.sp_crear_evaluacion_oposicion(
    p_id_postulacion   INTEGER,
    p_tema_exposicion  VARCHAR,
    p_fecha_evaluacion DATE,
    p_hora_inicio      TIME,
    p_hora_fin         TIME,
    p_lugar            VARCHAR,
    p_estado           VARCHAR
)
RETURNS INTEGER AS $$
DECLARE
    v_id INTEGER;
BEGIN
    INSERT INTO postulacion.evaluacion_oposicion
        (id_postulacion, tema_exposicion, fecha_evaluacion, hora_inicio, hora_fin, lugar, estado)
    VALUES
        (p_id_postulacion, p_tema_exposicion, p_fecha_evaluacion, p_hora_inicio, p_hora_fin, p_lugar, p_estado)
    RETURNING id_evaluacion_oposicion INTO v_id;
    
    -- Actualizar estado de la postulación
    UPDATE postulacion.postulacion
    SET estado = 'EN_EVALUACION'
    WHERE id_postulacion = p_id_postulacion;
    
    RETURN v_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.sp_actualizar_evaluacion_oposicion(
    p_id               INTEGER,
    p_tema_exposicion  VARCHAR,
    p_fecha_evaluacion DATE,
    p_hora_inicio      TIME,
    p_hora_fin         TIME,
    p_lugar            VARCHAR,
    p_estado           VARCHAR
)
RETURNS INTEGER AS $$
BEGIN
    UPDATE postulacion.evaluacion_oposicion
    SET tema_exposicion = COALESCE(p_tema_exposicion, tema_exposicion),
        fecha_evaluacion = COALESCE(p_fecha_evaluacion, fecha_evaluacion),
        hora_inicio = COALESCE(p_hora_inicio, hora_inicio),
        hora_fin = COALESCE(p_hora_fin, hora_fin),
        lugar = COALESCE(p_lugar, lugar),
        estado = COALESCE(p_estado, estado)
    WHERE id_evaluacion_oposicion = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.sp_listar_evaluaciones_oposicion()
RETURNS TABLE (
    id_evaluacion_oposicion INTEGER,
    tema_exposicion         VARCHAR,
    fecha_evaluacion        DATE,
    estado                  VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT e.id_evaluacion_oposicion, e.tema_exposicion, e.fecha_evaluacion, e.estado
    FROM postulacion.evaluacion_oposicion e;
END;
$$ LANGUAGE plpgsql;

-- 2. Permisos para que la aplicación (app_user_default) pueda usar estas nuevas funciones
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_actualizar_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_listar_evaluaciones_oposicion() TO app_user_default;

-- Permisos sobre las tablas de evaluación y comisiones para el rol coordinador
GRANT SELECT, INSERT, UPDATE, DELETE ON postulacion.evaluacion_oposicion TO role_coordinador;
GRANT SELECT, INSERT, UPDATE, DELETE ON seguridad.usuario_comision TO role_coordinador;
GRANT SELECT, UPDATE ON postulacion.postulacion TO role_coordinador;

-- Permisos sobre las secuencias para que pueda insertar
GRANT USAGE, SELECT ON SEQUENCE postulacion.evaluacion_oposicion_id_evaluacion_oposicion_seq TO role_coordinador;
GRANT USAGE, SELECT ON SEQUENCE seguridad.usuario_comision_id_usuario_comision_seq TO role_coordinador;

-- Asegurarnos de que el coordinador pueda ejecutar los stored procedures relacionados
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO role_coordinador;
GRANT EXECUTE ON FUNCTION public.sp_actualizar_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO role_coordinador;
GRANT EXECUTE ON FUNCTION public.sp_listar_evaluaciones_oposicion() TO role_coordinador;
GRANT EXECUTE ON FUNCTION public.sp_crear_usuario_comision(INTEGER, INTEGER, INTEGER, VARCHAR, NUMERIC, NUMERIC, NUMERIC, DATE) TO role_coordinador;
GRANT EXECUTE ON FUNCTION public.sp_listar_evaluadores_comision(INTEGER) TO role_coordinador;
