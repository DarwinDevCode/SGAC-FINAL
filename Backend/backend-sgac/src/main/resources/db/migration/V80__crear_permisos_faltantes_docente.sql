-- ==============================================================================
-- V80: Permisos faltantes para role_docente en todos los esquemas relevantes
-- El rol role_docente necesita permisos completos de lectura/uso sobre
-- el esquema ayudantia y otros esquemas subyacentes que el backend consulta.
-- ==============================================================================

GRANT USAGE ON SCHEMA ayudantia TO role_docente;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ayudantia TO role_docente;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ayudantia TO role_docente;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ayudantia TO role_docente;

GRANT USAGE ON SCHEMA postulacion TO role_docente;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA postulacion TO role_docente;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA postulacion TO role_docente;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA postulacion TO role_docente;

GRANT USAGE ON SCHEMA academico TO role_docente;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA academico TO role_docente;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA academico TO role_docente;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA academico TO role_docente;

GRANT USAGE ON SCHEMA convocatoria TO role_docente;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA convocatoria TO role_docente;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA convocatoria TO role_docente;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA convocatoria TO role_docente;

GRANT USAGE ON SCHEMA seguridad TO role_docente;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA seguridad TO role_docente;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA seguridad TO role_docente;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA seguridad TO role_docente;

GRANT USAGE ON SCHEMA public TO role_docente;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO role_docente;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO role_docente;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO role_docente;

GRANT USAGE ON SCHEMA notificacion TO role_docente;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA notificacion TO role_docente;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA notificacion TO role_docente;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA notificacion TO role_docente;
