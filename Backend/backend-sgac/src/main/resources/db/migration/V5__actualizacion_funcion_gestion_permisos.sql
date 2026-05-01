CREATE OR REPLACE FUNCTION seguridad.fn_gestionar_permisos_elemento2(
    p_rol_bd character varying,
    p_esquema character varying,
    p_elemento character varying,
    p_categoria character varying,
    p_privilegio character varying,
    p_otorgar boolean)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE SECURITY DEFINER
AS $BODY$
DECLARE
    v_sql text;
    v_tipo_objeto text;
    v_cat character varying := upper(p_categoria);
    v_priv character varying := upper(p_privilegio);
BEGIN
    IF v_cat = 'ESQUEMA' THEN
        v_tipo_objeto := 'SCHEMA';
        IF p_otorgar THEN
            v_sql := format('GRANT %s ON %s %I TO %I', v_priv, v_tipo_objeto, p_esquema, p_rol_bd);
        ELSE
            v_sql := format('REVOKE %s ON %s %I FROM %I', v_priv, v_tipo_objeto, p_esquema, p_rol_bd);
        END IF;

        EXECUTE v_sql;
        RETURN true;
    END IF;

    IF v_cat IN ('TABLA', 'TABLAS', 'VISTA', 'VISTAS', 'VISTA_MATERIALIZADA', 'VISTAS MATERIALIZADAS') THEN
        v_tipo_objeto := 'TABLE';
    ELSIF v_cat IN ('FUNCION', 'FUNCIONES', 'PROCEDIMIENTO', 'PROCEDIMIENTOS') THEN
        v_tipo_objeto := 'ROUTINE';
    ELSIF v_cat IN ('SECUENCIA', 'SECUENCIAS') THEN
        v_tipo_objeto := 'SEQUENCE';
    ELSE
        RAISE EXCEPTION 'Categoría no soportada: %', p_categoria;
    END IF;

    IF v_priv NOT IN ('SELECT', 'INSERT', 'UPDATE', 'DELETE', 'TRUNCATE', 'EXECUTE', 'USAGE', 'CREATE', 'REFERENCES', 'TRIGGER', 'ALL') THEN
        RAISE EXCEPTION 'Privilegio no válido: %', p_privilegio;
    END IF;

    IF p_otorgar THEN
        v_sql := format('GRANT %s ON %s %I.%I TO %I', v_priv, v_tipo_objeto, p_esquema, p_elemento, p_rol_bd);
    ELSE
        v_sql := format('REVOKE %s ON %s %I.%I FROM %I', v_priv, v_tipo_objeto, p_esquema, p_elemento, p_rol_bd);
    END IF;

    EXECUTE v_sql;
    RETURN true;

EXCEPTION WHEN OTHERS THEN
    RAISE WARNING 'Error al gestionar permiso: % - Objeto: %.% - Privilegio: %',
        SQLERRM, p_esquema, p_elemento, p_privilegio;
    RETURN false;
END;
$BODY$;