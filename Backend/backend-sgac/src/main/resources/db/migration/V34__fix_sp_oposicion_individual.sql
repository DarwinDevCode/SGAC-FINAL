-- V34__fix_sp_oposicion_individual.sql
-- Actualiza la definicion del prodecimiento para que coincida con la tabla

DROP FUNCTION IF EXISTS public.sp_actualizar_oposicion_individual(integer, numeric, numeric, numeric);
DROP FUNCTION IF EXISTS public.sp_guardar_oposicion_individual(integer, integer, character varying, numeric, numeric, numeric);

CREATE OR REPLACE FUNCTION public.sp_actualizar_oposicion_individual(
    p_id_calificacion integer,
    p_material numeric,
    p_calidad numeric,
    p_pertinencia numeric
)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE postulacion.calificacion_oposicion_individual
    SET
        criterio_material = p_material,
        criterio_calidad = p_calidad,
        criterio_pertinencia = p_pertinencia,
        fecha_registro = CURRENT_TIMESTAMP
    WHERE id_calificacion = p_id_calificacion;

    IF FOUND THEN
        RETURN p_id_calificacion;
    ELSE
        RETURN -1;
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        RETURN -1;
END;
$$;

CREATE OR REPLACE FUNCTION public.sp_guardar_oposicion_individual(
    p_id_postulacion integer,
    p_id_evaluador integer,
    p_rol character varying,
    p_material numeric,
    p_calidad numeric,
    p_pertinencia numeric
)
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_calificacion integer;
BEGIN
    INSERT INTO postulacion.calificacion_oposicion_individual (
        id_postulacion,
        id_evaluador,
        rol_evaluador,
        criterio_material,
        criterio_calidad,
        criterio_pertinencia,
        fecha_registro
    ) VALUES (
        p_id_postulacion,
        p_id_evaluador,
        p_rol,
        p_material,
        p_calidad,
        p_pertinencia,
        CURRENT_TIMESTAMP
    ) RETURNING id_calificacion INTO v_id_calificacion;

    RETURN v_id_calificacion;
EXCEPTION
    WHEN OTHERS THEN
        RETURN -1;
END;
$$;
