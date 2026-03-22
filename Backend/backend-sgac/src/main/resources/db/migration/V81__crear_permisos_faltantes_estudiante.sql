-- ==============================================================================
-- V81: Permisos faltantes para role_estudiante y role_ayudante_catedra
-- Evita errores 500 al consultar notificaciones y dashboard.
-- ==============================================================================

DO $$
DECLARE
    role_name text;
    schema_name text;
BEGIN
    FOR role_name IN SELECT unnest(ARRAY['role_estudiante', 'role_ayudante_catedra'])
    LOOP
        -- Solo aplicar si el rol ya existe
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = role_name) THEN
            FOR schema_name IN SELECT unnest(ARRAY['ayudantia', 'postulacion', 'academico', 'convocatoria', 'notificacion', 'seguridad', 'public'])
            LOOP
                EXECUTE format('GRANT USAGE ON SCHEMA %I TO %I', schema_name, role_name);
                EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO %I', schema_name, role_name);
                EXECUTE format('GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA %I TO %I', schema_name, role_name);
                EXECUTE format('GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA %I TO %I', schema_name, role_name);
            END LOOP;
        END IF;
    END LOOP;
END
$$;
