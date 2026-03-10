-- V38__fix_trigger_resumen.sql
-- Fix PostgreSQL error "transacción abortada" by converting the procedure into a function.
-- In PostgreSQL, calling a PROCEDURE from within a TRIGGER can cause transaction issues
-- (ERROR: invalid transaction termination) depending on the version and context.
-- A FUNCTION returning VOID is the standard way to execute complex logic safely from a trigger.

-- 1. Eliminar el procedimiento y el trigger anterior
DROP TRIGGER IF EXISTS tr_evaluacion_meritos_resumen ON postulacion.evaluacion_meritos;
DROP TRIGGER IF EXISTS tr_calificacion_oposicion_resumen ON postulacion.calificacion_oposicion_individual;
DROP FUNCTION IF EXISTS postulacion.fn_actualizar_resumen_evaluacion();
DROP PROCEDURE IF EXISTS postulacion.sp_recalcular_resumen_postulacion(INTEGER);

-- 2. Crear como FUNCIÓN (reemplazando al procedure)
CREATE OR REPLACE FUNCTION postulacion.fn_recalcular_resumen_postulacion(p_id_postulacion INTEGER)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_meritos NUMERIC(5,2) := 0;
    v_promedio_oposicion NUMERIC(5,2) := NULL;
    v_total_final NUMERIC(5,2) := 0;
    v_num_evaluadores INTEGER := 0;
BEGIN
    -- 1. Calcular Total Méritos
    SELECT COALESCE(SUM(nota_asignatura + nota_semestres + nota_eventos + nota_experiencia), 0)
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

-- 3. Función Trigger que llama a la nueva función
CREATE OR REPLACE FUNCTION postulacion.fn_actualizar_resumen_evaluacion()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        PERFORM postulacion.fn_recalcular_resumen_postulacion(OLD.id_postulacion);
    ELSE
        PERFORM postulacion.fn_recalcular_resumen_postulacion(NEW.id_postulacion);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 4. Recrear Triggers
CREATE TRIGGER tr_evaluacion_meritos_resumen
AFTER INSERT OR UPDATE OR DELETE ON postulacion.evaluacion_meritos
FOR EACH ROW EXECUTE FUNCTION postulacion.fn_actualizar_resumen_evaluacion();

CREATE TRIGGER tr_calificacion_oposicion_resumen
AFTER INSERT OR UPDATE OR DELETE ON postulacion.calificacion_oposicion_individual
FOR EACH ROW EXECUTE FUNCTION postulacion.fn_actualizar_resumen_evaluacion();

-- 5. Permisos
GRANT EXECUTE ON FUNCTION postulacion.fn_recalcular_resumen_postulacion(INTEGER) TO administrador_consultas;

-- Asegurarse también de dar permiso al rol_coordinador y al rol_docente/decano si es necesario
GRANT EXECUTE ON FUNCTION postulacion.fn_recalcular_resumen_postulacion(INTEGER) TO role_coordinador;

-- En PostgreSQL las funciones marcadas como SECURITY INVOKER (por defecto) se ejecutan 
-- con los permisos del usuario que provocó el trigger.
