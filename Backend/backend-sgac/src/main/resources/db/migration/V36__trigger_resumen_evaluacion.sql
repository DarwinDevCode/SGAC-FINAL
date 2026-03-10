-- V36__trigger_resumen_evaluacion.sql
-- Creación de procedimiento, triggers y backfill para mantener la tabla 'resumen_evaluacion' actualizada
-- automáticamente al registrar calificaciones de méritos y oposición.

-- 1. Procedimiento almacenado para recalcular o crear el resumen de una postulación
CREATE OR REPLACE PROCEDURE postulacion.sp_recalcular_resumen_postulacion(p_id_postulacion INTEGER)
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_meritos NUMERIC(5,2) := 0;
    v_promedio_oposicion NUMERIC(5,2) := NULL;
    v_total_final NUMERIC(5,2) := 0;
    v_num_evaluadores INTEGER := 0;
BEGIN
    -- 1. Calcular Total Méritos
    SELECT COALESCE(nota_asignatura, 0) + COALESCE(nota_semestres, 0) + COALESCE(nota_eventos, 0) + COALESCE(nota_experiencia, 0)
    INTO v_total_meritos
    FROM postulacion.evaluacion_meritos
    WHERE id_postulacion = p_id_postulacion;

    IF v_total_meritos IS NULL THEN
        v_total_meritos := 0;
    END IF;

    -- 2. Calcular Promedio Oposición
    SELECT COUNT(*), 
           ROUND(SUM(COALESCE(criterio_material, 0) + COALESCE(criterio_calidad, 0) + COALESCE(criterio_pertinencia, 0)) / 3.0, 2)
    INTO v_num_evaluadores, v_promedio_oposicion
    FROM postulacion.calificacion_oposicion_individual
    WHERE id_postulacion = p_id_postulacion;

    IF v_num_evaluadores < 3 THEN
        v_promedio_oposicion := NULL;
    END IF;

    -- 3. Calcular Total Final
    v_total_final := v_total_meritos + COALESCE(v_promedio_oposicion, 0);

    -- 4. Upsert en resumen_evaluacion
    INSERT INTO postulacion.resumen_evaluacion (
        id_postulacion, total_meritos, promedio_oposicion, total_final, fecha_calculo, estado
    ) VALUES (
        p_id_postulacion, v_total_meritos, v_promedio_oposicion, v_total_final, NOW(), 
        CASE WHEN v_num_evaluadores >= 3 THEN 
            (CASE WHEN v_total_final >= 25 THEN 'APTO' ELSE 'NO_APTO' END)
        ELSE 'PENDIENTE' END
    )
    ON CONFLICT (id_postulacion) DO UPDATE SET
        total_meritos = EXCLUDED.total_meritos,
        promedio_oposicion = EXCLUDED.promedio_oposicion,
        total_final = EXCLUDED.total_final,
        fecha_calculo = EXCLUDED.fecha_calculo,
        estado = CASE WHEN postulacion.resumen_evaluacion.estado IN ('GANADOR', 'DESIERTO') THEN postulacion.resumen_evaluacion.estado ELSE EXCLUDED.estado END;
END;
$$;

-- 2. Función Trigger que llama al procedimiento
CREATE OR REPLACE FUNCTION postulacion.fn_actualizar_resumen_evaluacion()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        CALL postulacion.sp_recalcular_resumen_postulacion(OLD.id_postulacion);
    ELSE
        CALL postulacion.sp_recalcular_resumen_postulacion(NEW.id_postulacion);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 3. Triggers en las tablas fuente
DROP TRIGGER IF EXISTS tr_evaluacion_meritos_resumen ON postulacion.evaluacion_meritos;
CREATE TRIGGER tr_evaluacion_meritos_resumen
AFTER INSERT OR UPDATE OR DELETE ON postulacion.evaluacion_meritos
FOR EACH ROW EXECUTE FUNCTION postulacion.fn_actualizar_resumen_evaluacion();

DROP TRIGGER IF EXISTS tr_calificacion_oposicion_resumen ON postulacion.calificacion_oposicion_individual;
CREATE TRIGGER tr_calificacion_oposicion_resumen
AFTER INSERT OR UPDATE OR DELETE ON postulacion.calificacion_oposicion_individual
FOR EACH ROW EXECUTE FUNCTION postulacion.fn_actualizar_resumen_evaluacion();

-- 4. Permisos
GRANT EXECUTE ON PROCEDURE postulacion.sp_recalcular_resumen_postulacion(INTEGER) TO administrador_consultas, coordinador_ayudantia;

-- 5. Backfill: Ejecutar el procedimiento para todos los registros existentes
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN 
        SELECT DISTINCT id_postulacion FROM postulacion.evaluacion_meritos
        UNION
        SELECT DISTINCT id_postulacion FROM postulacion.calificacion_oposicion_individual
    LOOP
        CALL postulacion.sp_recalcular_resumen_postulacion(r.id_postulacion);
    END LOOP;
END;
$$;
