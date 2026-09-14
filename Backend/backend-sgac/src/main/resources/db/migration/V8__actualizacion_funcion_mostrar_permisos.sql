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
    VOLATILE SECURITY DEFINER PARALLEL UNSAFE
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
END;
$BODY$;


CREATE OR REPLACE FUNCTION seguridad.fn_consultar_permisos_rol(
    p_rol_bd VARCHAR,
    p_esquema VARCHAR DEFAULT 'todo',
    p_categoria VARCHAR DEFAULT 'todo',
    p_privilegio VARCHAR DEFAULT 'todo'
)
    RETURNS TABLE(esquema varchar, elemento varchar, categoria varchar, privilegio varchar)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $BODY$
BEGIN
    RETURN QUERY
        WITH acl_data AS (
            -- 1. Permisos de Tablas y Vistas
            SELECT
                n.nspname::varchar as esc,
                c.relname::varchar as elm,
                CASE
                    WHEN c.relkind = 'r' THEN 'TABLA'
                    WHEN c.relkind = 'v' THEN 'VISTA'
                    WHEN c.relkind = 'm' THEN 'VISTA_MATERIALIZADA'
                    ELSE 'OTRO'
                    END::text as cat, -- Lo tratamos como text internamente
                (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).grantee as grantee_oid,
                (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).privilege_type::text as priv
            FROM pg_class c
                     JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relkind IN ('r', 'v', 'm')
              AND n.nspname NOT IN ('pg_catalog', 'information_schema')

            UNION ALL

            -- 2. Permisos de Funciones y Procedimientos
            SELECT
                n.nspname::varchar as esc,
                p.proname::varchar as elm,
                CASE WHEN p.prokind = 'p' THEN 'PROCEDIMIENTO' ELSE 'FUNCION' END::text as cat,
                (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).grantee as grantee_oid,
                (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).privilege_type::text as priv
            FROM pg_proc p
                     JOIN pg_namespace n ON n.oid = p.pronamespace
            WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
        )
        SELECT
            ad.esc::varchar,     -- Forzamos casting final a varchar
            ad.elm::varchar,     -- Forzamos casting final a varchar
            ad.cat::varchar,     -- Esto resuelve el error en la posición 3
            ad.priv::varchar     -- Forzamos casting final a varchar
        FROM acl_data ad
        WHERE lower(pg_get_userbyid(ad.grantee_oid)) = lower(p_rol_bd)
          AND (p_esquema = 'todo' OR lower(ad.esc) = lower(p_esquema))
          AND (p_categoria = 'todo' OR lower(ad.cat) = lower(p_categoria))
          AND (p_privilegio = 'todo' OR lower(ad.priv) = lower(p_privilegio))
        ORDER BY ad.esc, ad.elm, ad.priv;
END;
$BODY$;