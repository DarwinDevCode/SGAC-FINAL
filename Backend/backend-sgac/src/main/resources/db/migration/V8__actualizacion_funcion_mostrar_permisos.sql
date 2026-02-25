DROP FUNCTION IF EXISTS seguridad.fn_consultar_permisos_rol(VARCHAR, VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS seguridad.fn_gestionar_permisos_elemento2(character varying, character varying, character varying, character varying, character varying, boolean);



CREATE OR REPLACE FUNCTION seguridad.fn_consultar_permisos_rol(
    p_rol_bd VARCHAR,
    p_esquema VARCHAR DEFAULT 'Todos',
    p_categoria VARCHAR DEFAULT 'Todas',
    p_privilegio VARCHAR DEFAULT 'Todas'
)
    RETURNS TABLE(
                     esquema VARCHAR,
                     elemento VARCHAR,
                     categoria VARCHAR,
                     privilegio VARCHAR
                 )
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
        WITH objetos AS (
            SELECT
                t.table_schema::VARCHAR AS esc,
                t.table_name::VARCHAR AS elm,
                CASE
                    WHEN t.table_type = 'BASE TABLE' THEN 'TABLA'
                    WHEN t.table_type = 'VIEW' THEN 'VISTA'
                    END AS tipo
            FROM information_schema.tables t
            UNION ALL
            SELECT
                schemaname::VARCHAR AS esc,
                matviewname::VARCHAR AS elm,
                'VISTA_MATERIALIZADA' AS tipo
            FROM pg_matviews
            UNION ALL
            SELECT
                r.routine_schema::VARCHAR AS esc,
                r.routine_name::VARCHAR AS elm,
                CASE
                    WHEN r.routine_type = 'FUNCTION' THEN 'FUNCION'
                    WHEN r.routine_type = 'PROCEDURE' THEN 'PROCEDIMIENTO'
                    END AS tipo
            FROM information_schema.routines r
        ),
             permisos_normalizados AS (
                 SELECT
                     o.esc,
                     o.elm,
                     tos.nombre_tipo_objeto::VARCHAR AS cat,
                     p.privilege_type::VARCHAR AS priv
                 FROM objetos o
                          JOIN seguridad.tipo_objeto_seguridad tos ON tos.nombre_tipo_objeto = o.tipo
                          LEFT JOIN information_schema.role_table_grants p ON p.table_schema = o.esc
                     AND p.table_name = o.elm
                     AND p.grantee = p_rol_bd
                 UNION ALL
                 SELECT
                     o.esc,
                     o.elm,
                     tos.nombre_tipo_objeto::VARCHAR AS cat,
                     p.privilege_type::VARCHAR AS priv
                 FROM objetos o
                          JOIN seguridad.tipo_objeto_seguridad tos ON tos.nombre_tipo_objeto = o.tipo
                          LEFT JOIN information_schema.role_routine_grants p ON p.routine_schema = o.esc
                     AND p.routine_name = o.elm
                     AND p.grantee = p_rol_bd
             )
        SELECT DISTINCT
            pn.esc::VARCHAR,
            pn.elm::VARCHAR,
            pn.cat::VARCHAR,
            pn.priv::VARCHAR
        FROM permisos_normalizados pn
        WHERE pn.priv IS NOT NULL
          AND (p_esquema IS NULL OR lower(p_esquema) = 'todos' OR pn.esc = p_esquema)
          AND (p_categoria IS NULL OR lower(p_categoria) = 'todas' OR pn.cat = p_categoria)
          AND (p_privilegio IS NULL OR lower(p_privilegio) = 'todas' OR pn.priv = p_privilegio)
        ORDER BY pn.esc, pn.elm;
END;
$$;



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