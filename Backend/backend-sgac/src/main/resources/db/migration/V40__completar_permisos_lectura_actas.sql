-- Otorgar USAGE a esquemas extra por si existen referencias secundarias
GRANT USAGE ON SCHEMA ayudantia TO role_coordinador, role_docente;

-- Para evitar más errores por la carga perezosa o ansiosa de dependencias de Hibernate (como usuario_tipo_rol, PeriodoAcademico, etc)
-- se otorgan permisos de solo lectura a TODAS las tablas de los esquemas involucrados en este módulo a los roles evaluadores.
GRANT SELECT ON ALL TABLES IN SCHEMA seguridad TO role_coordinador, role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA academico TO role_coordinador, role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA postulacion TO role_coordinador, role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA convocatoria TO role_coordinador, role_docente;
GRANT SELECT ON ALL TABLES IN SCHEMA ayudantia TO role_coordinador, role_docente;
