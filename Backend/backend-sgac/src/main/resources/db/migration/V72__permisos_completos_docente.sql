-- V72: Grant all required schema permissions to role_docente
-- This fixes the 'permiso denegado a la tabla comision_seleccion' error
-- and any other 'permiso denegado' errors on the Docente Dashboard.

DO $$
DECLARE
    schema_name TEXT;
BEGIN
    FOR schema_name IN 
        SELECT unnest(ARRAY['postulacion', 'academico', 'convocatoria', 'seguridad', 'public'])
    LOOP
        EXECUTE format('GRANT USAGE ON SCHEMA %I TO role_docente;', schema_name);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO role_docente;', schema_name);
        EXECUTE format('GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA %I TO role_docente;', schema_name);
        EXECUTE format('GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA %I TO role_docente;', schema_name);
    END LOOP;
END $$;
