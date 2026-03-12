create or replace function public.sp_crear_postulacion(p_id_convocatoria integer, p_id_estudiante integer, p_fecha_postulacion date, p_estado_postulacion character varying, p_observaciones character varying) returns integer
    language plpgsql
as
$$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO postulacion.postulacion (
        id_convocatoria, id_estudiante,
        fecha_postulacion, id_tipo_estado_postulacion, activo
    ) VALUES (
                 p_id_convocatoria, p_id_estudiante,
                 p_fecha_postulacion, (select id_tipo_estado_postulacion from postulacion.tipo_estado_postulacion where codigo = 'PENDIENTE' limit 1), TRUE
             ) RETURNING id_postulacion INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error en sp_crear_postulacion: %', SQLERRM;
    RETURN -1;
END;
$$;
