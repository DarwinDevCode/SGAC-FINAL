-- V37__fix_sp_evaluacion_oposicion_estado.sql
-- Fix column typo in sp_crear_evaluacion_oposicion

DROP FUNCTION IF EXISTS public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR);

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
    -- FIX: Cambiar "estado" por "estado_postulacion"
    UPDATE postulacion.postulacion
    SET estado_postulacion = 'EN_EVALUACION'
    WHERE id_postulacion = p_id_postulacion;
    
    RETURN v_id;
EXCEPTION
    WHEN OTHERS THEN
        -- It's better to raise the exception to see what actually failed
        RAISE NOTICE 'Error in sp_crear_evaluacion_oposicion: %', SQLERRM;
        RETURN -1;
END;
$$ LANGUAGE plpgsql;

-- Asegurarnos de que el coordinador pueda ejecutar el stored procedure
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO role_coordinador;
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO app_user_default;
