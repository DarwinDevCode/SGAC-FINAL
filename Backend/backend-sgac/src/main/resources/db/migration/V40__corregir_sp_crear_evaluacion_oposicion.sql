-- V40: Corregir sp_crear_evaluacion_oposicion y asegurar estado EN_EVALUACION
-- 1. Asegurar que el estado EN_EVALUACION exista en el catálogo
INSERT INTO postulacion.tipo_estado_postulacion (codigo, nombre, descripcion)
VALUES ('EN_EVALUACION', 'En Evaluación', 'La postulación está en proceso de evaluación de oposición y méritos')
ON CONFLICT (codigo) DO NOTHING;

-- 2. Corregir el Stored Procedure sp_crear_evaluacion_oposicion
-- Se corrige el nombre de la columna 'estado' a 'estado_postulacion'
-- Se actualiza id_tipo_estado_postulacion para mantener consistencia
-- Se elimina el bloque EXCEPTION silencioso para permitir depuración
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
    v_id_estado_evaluacion INTEGER;
BEGIN
    -- Obtenemos el ID del estado EN_EVALUACION
    SELECT id_tipo_estado_postulacion INTO v_id_estado_evaluacion
    FROM postulacion.tipo_estado_postulacion
    WHERE codigo = 'EN_EVALUACION';

    -- Insertar en evaluacion_oposicion
    INSERT INTO postulacion.evaluacion_oposicion
        (id_postulacion, tema_exposicion, fecha_evaluacion, hora_inicio, hora_fin, lugar, estado)
    VALUES
        (p_id_postulacion, p_tema_exposicion, p_fecha_evaluacion, p_hora_inicio, p_hora_fin, p_lugar, p_estado)
    RETURNING id_evaluacion_oposicion INTO v_id;
    
    -- Actualizar estado de la postulación (CORREGIDO: estado_postulacion e id_tipo_estado_postulacion)
    UPDATE postulacion.postulacion
    SET estado_postulacion = 'EN_EVALUACION',
        id_tipo_estado_postulacion = v_id_estado_evaluacion
    WHERE id_postulacion = p_id_postulacion;
    
    RETURN v_id;
END;
$$ LANGUAGE plpgsql;

-- 3. Asegurar permisos de ejecución
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO role_coordinador;
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO app_user_default;
