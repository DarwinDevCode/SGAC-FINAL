CREATE OR REPLACE FUNCTION seguridad.fn_crear_roles_iniciales()
    RETURNS void
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'role_administrador') THEN
        CREATE ROLE role_administrador NOLOGIN;

    END IF;
END;
$$;

SELECT seguridad.fn_crear_roles_iniciales();