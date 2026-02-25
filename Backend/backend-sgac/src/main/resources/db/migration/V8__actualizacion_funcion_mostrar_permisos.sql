DROP FUNCTION IF EXISTS seguridad.fn_consultar_permisos_rol(VARCHAR, VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS seguridad.fn_gestionar_permisos_elemento2(character varying, character varying, character varying, character varying, character varying, boolean);



-- CREATE OR REPLACE FUNCTION seguridad.fn_consultar_permisos_rol(
--     p_rol_bd VARCHAR,
--     p_esquema VARCHAR DEFAULT 'todo',
--     p_categoria VARCHAR DEFAULT 'todo',
--     p_privilegio VARCHAR DEFAULT 'todo'
-- )
--     RETURNS TABLE(
--                      esquema VARCHAR,
--                      elemento VARCHAR,
--                      categoria VARCHAR,
--                      privilegio VARCHAR
--                  )
--     LANGUAGE plpgsql
--     SECURITY DEFINER
-- AS $$
-- BEGIN
--     RETURN QUERY
--         WITH objetos AS (
--             SELECT
--                 t.table_schema::VARCHAR AS esc,
--                 t.table_name::VARCHAR AS elm,
--                 CASE
--                     WHEN t.table_type = 'BASE TABLE' THEN 'TABLA'
--                     WHEN t.table_type = 'VIEW' THEN 'VISTA'
--                     END AS tipo
--             FROM information_schema.tables t
--             UNION ALL
--             SELECT
--                 schemaname::VARCHAR AS esc,
--                 matviewname::VARCHAR AS elm,
--                 'VISTA_MATERIALIZADA' AS tipo
--             FROM pg_matviews
--             UNION ALL
--             SELECT
--                 r.routine_schema::VARCHAR AS esc,
--                 r.routine_name::VARCHAR AS elm,
--                 CASE
--                     WHEN r.routine_type = 'FUNCTION' THEN 'FUNCION'
--                     WHEN r.routine_type = 'PROCEDURE' THEN 'PROCEDIMIENTO'
--                     END AS tipo
--             FROM information_schema.routines r
--         ),
--              permisos_normalizados AS (
--                  SELECT
--                      o.esc,
--                      o.elm,
--                      tos.nombre_tipo_objeto::VARCHAR AS cat,
--                      p.privilege_type::VARCHAR AS priv
--                  FROM objetos o
--                           JOIN seguridad.tipo_objeto_seguridad tos ON tos.nombre_tipo_objeto = o.tipo
--                           LEFT JOIN information_schema.role_table_grants p ON p.table_schema = o.esc
--                      AND p.table_name = o.elm
--                      AND p.grantee = p_rol_bd
--                  UNION ALL
--                  SELECT
--                      o.esc,
--                      o.elm,
--                      tos.nombre_tipo_objeto::VARCHAR AS cat,
--                      p.privilege_type::VARCHAR AS priv
--                  FROM objetos o
--                           JOIN seguridad.tipo_objeto_seguridad tos ON tos.nombre_tipo_objeto = o.tipo
--                           LEFT JOIN information_schema.role_routine_grants p ON p.routine_schema = o.esc
--                      AND p.routine_name = o.elm
--                      AND p.grantee = p_rol_bd
--              )
--         SELECT DISTINCT
--             pn.esc::VARCHAR,
--             pn.elm::VARCHAR,
--             pn.cat::VARCHAR,
--             pn.priv::VARCHAR
--         FROM permisos_normalizados pn
--         WHERE pn.priv IS NOT NULL
--           AND (p_esquema IS NULL OR lower(p_esquema) = 'todo' OR pn.esc = p_esquema)
--           AND (p_categoria IS NULL OR lower(p_categoria) = 'todo' OR pn.cat = p_categoria)
--           AND (p_privilegio IS NULL OR lower(p_privilegio) = 'todo' OR pn.priv = p_privilegio)
--         ORDER BY pn.esc, pn.elm;
-- END;
-- $$;



-- CREATE OR REPLACE FUNCTION seguridad.fn_consultar_permisos_rol(
--     p_rol_bd VARCHAR,
--     p_esquema VARCHAR DEFAULT 'todo',
--     p_categoria VARCHAR DEFAULT 'todo',
--     p_privilegio VARCHAR DEFAULT 'todo'
-- )
--     RETURNS TABLE(esquema varchar, elemento varchar, categoria varchar, privilegio varchar)
--     LANGUAGE plpgsql
--     SECURITY DEFINER
-- AS $BODY$
-- BEGIN
--     RETURN QUERY
--         WITH acl_data AS (
--             SELECT
--                 n.nspname::varchar as esc,
--                 c.relname::varchar as elm,
--                 CASE
--                     WHEN c.relkind = 'r' THEN 'TABLA'
--                     WHEN c.relkind = 'v' THEN 'VISTA'
--                     WHEN c.relkind = 'm' THEN 'VISTA_MATERIALIZADA'
--                     ELSE 'OTRO'
--                     END as cat,
--                 (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).grantee as grantee_oid,
--                 (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).privilege_type::varchar as priv
--             FROM pg_class c
--                      JOIN pg_namespace n ON n.oid = c.relnamespace
--             WHERE c.relkind IN ('r', 'v', 'm')
--
--             UNION ALL
--
--             SELECT
--                 n.nspname::varchar as esc,
--                 p.proname::varchar as elm,
--                 CASE WHEN p.prokind = 'p' THEN 'PROCEDIMIENTO' ELSE 'FUNCION' END as cat,
--                 (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).grantee as grantee_oid,
--                 (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).privilege_type::varchar as priv
--             FROM pg_proc p
--                      JOIN pg_namespace n ON n.oid = p.pronamespace
--         )
--         SELECT
--             ad.esc,
--             ad.elm,
--             ad.cat,
--             ad.priv
--         FROM acl_data ad
--                  JOIN pg_authid u ON u.oid = ad.grantee_oid
--         WHERE u.rolname = p_rol_bd
--           AND (p_esquema = 'todo' OR ad.esc = p_esquema)
--           AND (p_categoria = 'todo' OR ad.cat = p_categoria)
--           AND (p_privilegio = 'todo' OR ad.priv = p_privilegio)
--         ORDER BY ad.esc, ad.elm, ad.priv;
-- END;
-- $BODY$;



-- CREATE OR REPLACE FUNCTION seguridad.fn_consultar_permisos_rol(
--     p_rol_bd VARCHAR,
--     p_esquema VARCHAR DEFAULT 'todo',
--     p_categoria VARCHAR DEFAULT 'todo',
--     p_privilegio VARCHAR DEFAULT 'todo'
-- )
--     RETURNS TABLE(esquema varchar, elemento varchar, categoria varchar, privilegio varchar)
--     LANGUAGE plpgsql
--     SECURITY DEFINER
-- AS $BODY$
-- BEGIN
--     RETURN QUERY
--         WITH acl_data AS (
--             SELECT
--                 n.nspname::varchar as esc,
--                 c.relname::varchar as elm,
--                 CASE
--                     WHEN c.relkind = 'r' THEN 'TABLA'
--                     WHEN c.relkind = 'v' THEN 'VISTA'
--                     WHEN c.relkind = 'm' THEN 'VISTA_MATERIALIZADA'
--                     ELSE 'OTRO'
--                     END as cat,
--                 (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).grantee as grantee_oid,
--                 (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).privilege_type::varchar as priv
--             FROM pg_class c
--                      JOIN pg_namespace n ON n.oid = c.relnamespace
--             WHERE c.relkind IN ('r', 'v', 'm')
--
--             UNION ALL
--
--             SELECT
--                 n.nspname::varchar as esc,
--                 p.proname::varchar as elm,
--                 CASE WHEN p.prokind = 'p' THEN 'PROCEDIMIENTO' ELSE 'FUNCION' END as cat,
--                 (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).grantee as grantee_oid,
--                 (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).privilege_type::varchar as priv
--             FROM pg_proc p
--                      JOIN pg_namespace n ON n.oid = p.pronamespace
--         )
--         SELECT
--             ad.esc,
--             ad.elm,
--             ad.cat,
--             ad.priv
--         FROM acl_data ad
--                  JOIN pg_roles r ON r.oid = ad.grantee_oid
--         WHERE r.rolname = p_rol_bd
--           AND (p_esquema = 'todo' OR ad.esc = p_esquema)
--           AND (p_categoria = 'todo' OR ad.cat = p_categoria)
--           AND (p_privilegio = 'todo' OR ad.priv = p_privilegio)
--         ORDER BY ad.esc, ad.elm, ad.priv;
-- END;
-- $BODY$;

-- CREATE OR REPLACE FUNCTION seguridad.fn_consultar_permisos_rol(
--     p_rol_bd VARCHAR,
--     p_esquema VARCHAR DEFAULT 'todo',
--     p_categoria VARCHAR DEFAULT 'todo',
--     p_privilegio VARCHAR DEFAULT 'todo'
-- )
--     RETURNS TABLE(esquema varchar, elemento varchar, categoria varchar, privilegio varchar)
--     LANGUAGE plpgsql
--     SECURITY DEFINER
-- AS $BODY$
-- BEGIN
--     RETURN QUERY
--         WITH acl_data AS (
--             -- 1. Permisos de Tablas y Vistas
--             SELECT
--                 n.nspname::varchar as esc,
--                 c.relname::varchar as elm,
--                 CASE
--                     WHEN c.relkind = 'r' THEN 'TABLA'
--                     WHEN c.relkind = 'v' THEN 'VISTA'
--                     WHEN c.relkind = 'm' THEN 'VISTA_MATERIALIZADA'
--                     ELSE 'OTRO'
--                     END as cat,
--                 (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).grantee as grantee_oid,
--                 (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).privilege_type::varchar as priv
--             FROM pg_class c
--                      JOIN pg_namespace n ON n.oid = c.relnamespace
--             WHERE c.relkind IN ('r', 'v', 'm')
--               AND n.nspname NOT IN ('pg_catalog', 'information_schema')
--
--             UNION ALL
--
--             -- 2. Permisos de Funciones y Procedimientos
--             SELECT
--                 n.nspname::varchar as esc,
--                 p.proname::varchar as elm,
--                 CASE WHEN p.prokind = 'p' THEN 'PROCEDIMIENTO' ELSE 'FUNCION' END as cat,
--                 (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).grantee as grantee_oid,
--                 (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).privilege_type::varchar as priv
--             FROM pg_proc p
--                      JOIN pg_namespace n ON n.oid = p.pronamespace
--             WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
--         )
--         SELECT
--             ad.esc,
--             ad.elm,
--             ad.cat,
--             ad.priv
--         FROM acl_data ad
--         -- Usamos la función interna de Postgres para obtener el nombre del rol por su OID
--         WHERE lower(pg_get_userbyid(ad.grantee_oid)) = lower(p_rol_bd)
--           AND (p_esquema = 'todo' OR lower(ad.esc) = lower(p_esquema))
--           AND (p_categoria = 'todo' OR lower(ad.cat) = lower(p_categoria))
--           AND (p_privilegio = 'todo' OR lower(ad.priv) = lower(p_privilegio))
--         ORDER BY ad.esc, ad.elm, ad.priv;
-- END;
-- $BODY$;

-- IMPORTANTE: Asegúrate de que el usuario que usa Spring Boot tenga permiso para ejecutarla
-- GRANT EXECUTE ON FUNCTION seguridad.fn_consultar_permisos_rol(varchar, varchar, varchar, varchar) TO public;


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