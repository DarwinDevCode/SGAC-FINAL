CREATE OR REPLACE FUNCTION seguridad.fn_listar_esquemas_bd()
    RETURNS TABLE (
                      nombre_esquema VARCHAR
                  )
    LANGUAGE sql
AS $$
SELECT
    n.nspname
FROM pg_namespace n
WHERE n.nspname NOT LIKE 'pg_%'
  AND n.nspname <> 'information_schema'
ORDER BY n.nspname;
$$;




CREATE OR REPLACE FUNCTION seguridad.fn_listar_tipos_objeto_seguridad()
    RETURNS TABLE (
                      id_tipo_objeto_seguridad INTEGER,
                      nombre_tipo_objeto VARCHAR
                  )
    LANGUAGE sql
AS $$
SELECT
    t.id_tipo_objeto_seguridad,
    t.nombre_tipo_objeto
FROM seguridad.tipo_objeto_seguridad t
ORDER BY t.id_tipo_objeto_seguridad;
$$;



CREATE OR REPLACE FUNCTION seguridad.fn_listar_elementos_por_tipo_de_objeto(
    p_esquema VARCHAR,
    p_tipo_objeto VARCHAR
)
    RETURNS TABLE (nombre_elemento VARCHAR)
    LANGUAGE plpgsql
AS $$
BEGIN

    IF UPPER(p_tipo_objeto) = 'TABLA' THEN
        RETURN QUERY
            SELECT table_name::VARCHAR
            FROM information_schema.tables
            WHERE table_schema = p_esquema
              AND table_type = 'BASE TABLE'
            ORDER BY table_name;

    ELSIF UPPER(p_tipo_objeto) = 'VISTA' THEN
        RETURN QUERY
            SELECT table_name::VARCHAR
            FROM information_schema.tables
            WHERE table_schema = p_esquema
              AND table_type = 'VIEW'
            ORDER BY table_name;

    ELSIF UPPER(p_tipo_objeto) = 'VISTA_MATERIALIZADA' THEN
        RETURN QUERY
            SELECT matviewname::VARCHAR
            FROM pg_matviews
            WHERE schemaname = p_esquema
            ORDER BY matviewname;

    ELSIF UPPER(p_tipo_objeto) = 'FUNCION' THEN
        RETURN QUERY
            SELECT routine_name::VARCHAR
            FROM information_schema.routines
            WHERE routine_schema = p_esquema
              AND routine_type = 'FUNCTION'
            ORDER BY routine_name;

    ELSIF UPPER(p_tipo_objeto) = 'PROCEDIMIENTO' THEN
        RETURN QUERY
            SELECT routine_name::VARCHAR
            FROM information_schema.routines
            WHERE routine_schema = p_esquema
              AND routine_type = 'PROCEDURE'
            ORDER BY routine_name;

    ELSIF UPPER(p_tipo_objeto) = 'ESQUEMA' THEN
        RETURN QUERY
            SELECT schema_name::VARCHAR
            FROM information_schema.schemata
            WHERE schema_name = p_esquema;

    ELSE
        RAISE EXCEPTION 'Tipo de objeto no válido: %', p_tipo_objeto;
    END IF;

END;
$$;



CREATE OR REPLACE FUNCTION seguridad.fn_listar_privilegios_por_tipo_objeto(
    p_id_tipo_objeto INT
)
    RETURNS TABLE (
                      id_privilegio INT,
                      nombre_privilegio VARCHAR,
                      codigo_interno CHAR(1)
                  )
    LANGUAGE sql
AS $$
SELECT
    p.id_privilegio,
    p.nombre_privilegio,
    p.codigo_interno
FROM seguridad.tipo_objeto_seguridad_privilegio tp
         INNER JOIN seguridad.privilegio p
                    ON tp.id_privilegio = p.id_privilegio
WHERE tp.id_tipo_objeto_seguridad = p_id_tipo_objeto
ORDER BY p.nombre_privilegio;
$$;