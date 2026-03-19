-- V63: Garantizar permisos para administrador_consultas sobre esquemas del sistema
-- Necesario para funciones SECURITY DEFINER que acceden a tablas de otros esquemas.

DO $$
BEGIN
    -- 1. USAGE en Schemas
    GRANT USAGE ON SCHEMA academico    TO administrador_consultas;
    GRANT USAGE ON SCHEMA ayudantia     TO administrador_consultas;
    GRANT USAGE ON SCHEMA planificacion TO administrador_consultas;
    GRANT USAGE ON SCHEMA seguridad     TO administrador_consultas;
    GRANT USAGE ON SCHEMA convocatoria  TO administrador_consultas;
    GRANT USAGE ON SCHEMA postulacion   TO administrador_consultas;
    GRANT USAGE ON SCHEMA public        TO administrador_consultas;

    -- 2. Permisos DML en tablas de cada esquema
    -- Se otorga SELECT para lectura y DML para funciones que realizan cambios
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA academico    TO administrador_consultas;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ayudantia     TO administrador_consultas;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA planificacion TO administrador_consultas;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA seguridad     TO administrador_consultas;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA convocatoria  TO administrador_consultas;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA postulacion   TO administrador_consultas;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public        TO administrador_consultas;

    -- 3. Permisos en Secuencias (necesarios para INSERTs)
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA academico    TO administrador_consultas;
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ayudantia     TO administrador_consultas;
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA planificacion TO administrador_consultas;
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA seguridad     TO administrador_consultas;
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA convocatoria  TO administrador_consultas;
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA postulacion   TO administrador_consultas;
    GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public        TO administrador_consultas;

END $$;
