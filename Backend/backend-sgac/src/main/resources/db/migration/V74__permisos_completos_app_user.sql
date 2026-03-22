-- V74: Grant all required schema permissions to app_user_default
-- Fixes permission denied errors inside SECURITY DEFINER functions owned by app_user_default.

DO $$
DECLARE
    schema_name TEXT;
    target_role TEXT;
BEGIN
    FOR target_role IN SELECT unnest(ARRAY['app_user_default', 'administrador_consultas'])
    LOOP
        FOR schema_name IN 
            SELECT unnest(ARRAY['ayudantia', 'postulacion', 'academico', 'convocatoria', 'notificacion', 'seguridad', 'public'])
        LOOP
            EXECUTE format('GRANT USAGE ON SCHEMA %I TO %I;', schema_name, target_role);
            EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO %I;', schema_name, target_role);
            EXECUTE format('GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA %I TO %I;', schema_name, target_role);
            EXECUTE format('GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA %I TO %I;', schema_name, target_role);
        END LOOP;
    END LOOP;
END $$;
