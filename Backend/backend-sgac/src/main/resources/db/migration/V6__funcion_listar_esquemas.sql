CREATE OR REPLACE FUNCTION seguridad.fn_listar_esquemas()
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
