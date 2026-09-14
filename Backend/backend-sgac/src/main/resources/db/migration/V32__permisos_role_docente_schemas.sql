-- V32: Permisos de role_docente sobre todos los schemas referenciados por los SPs docente
-- Los SPs sp_resumen_docente y sp_listar_ayudantes_docente hacen JOINs a múltiples schemas

-- ── schemas adicionales que necesita role_docente ──────────────────────────
GRANT USAGE ON SCHEMA convocatoria  TO role_docente;
GRANT USAGE ON SCHEMA postulacion   TO role_docente;
GRANT USAGE ON SCHEMA academico     TO role_docente;
GRANT USAGE ON SCHEMA seguridad     TO role_docente;
GRANT USAGE ON SCHEMA public        TO role_docente;

-- ── SELECT en tablas de cada schema ───────────────────────────────────────
GRANT SELECT ON ALL TABLES IN SCHEMA convocatoria  TO role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA postulacion   TO role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA academico     TO role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA seguridad     TO role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA public        TO role_docente;
