-- Actualizar función para crear tipo de requisito
CREATE OR REPLACE FUNCTION public.fn_crear_tipo_requisito_postulacion(
    p_nombre character varying,
    p_descripcion text,
    p_tipo_documento_permitido character varying DEFAULT NULL::character varying
)
RETURNS integer
LANGUAGE plpgsql
AS $function$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO convocatoria.tipo_requisito_postulacion (nombre_requisito, descripcion, activo, tipo_documento_permitido)
    VALUES (p_nombre, p_descripcion, true, p_tipo_documento_permitido)
    RETURNING id_tipo_requisito_postulacion INTO v_id;
    
    RETURN v_id;
END;
$function$;


-- Actualizar función para actualizar tipo de requisito
CREATE OR REPLACE FUNCTION public.fn_actualizar_tipo_requisito_postulacion(
    p_id integer,
    p_nombre character varying,
    p_descripcion text,
    p_tipo_documento_permitido character varying DEFAULT NULL::character varying
)
RETURNS integer
LANGUAGE plpgsql
AS $function$
BEGIN
    UPDATE convocatoria.tipo_requisito_postulacion
    SET nombre_requisito = p_nombre,
        descripcion = p_descripcion,
        tipo_documento_permitido = p_tipo_documento_permitido
    WHERE id_tipo_requisito_postulacion = p_id;
    
    RETURN p_id;
END;
$function$;
