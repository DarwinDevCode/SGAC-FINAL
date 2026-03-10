-- Permisos sobre esquemas base para roles evaluadores
GRANT USAGE ON SCHEMA seguridad TO role_coordinador, role_docente;
GRANT USAGE ON SCHEMA academico TO role_coordinador, role_docente;
GRANT USAGE ON SCHEMA postulacion TO role_coordinador, role_docente;
GRANT USAGE ON SCHEMA convocatoria TO role_coordinador, role_docente;

-- Permisos de solo lectura sobre las tablas consultadas durante la generacion del acta PDF
GRANT SELECT ON seguridad.usuario TO role_coordinador, role_docente;
GRANT SELECT ON seguridad.tipo_rol TO role_coordinador, role_docente;
GRANT SELECT ON seguridad.usuario_tipo_rol TO role_coordinador, role_docente;
GRANT SELECT ON academico.carrera TO role_coordinador, role_docente;
GRANT SELECT ON academico.asignatura TO role_coordinador, role_docente;
GRANT SELECT ON academico.facultad TO role_coordinador, role_docente;
GRANT SELECT ON academico.docente TO role_coordinador, role_docente;
GRANT SELECT ON academico.estudiante TO role_coordinador, role_docente;

-- Permisos sobre tablas de evaluación y postulación (por si faltaban)
GRANT SELECT ON postulacion.postulacion TO role_coordinador, role_docente;
GRANT SELECT ON convocatoria.convocatoria TO role_coordinador, role_docente;
GRANT SELECT ON postulacion.acta_evaluacion TO role_coordinador, role_docente;
GRANT SELECT ON postulacion.resumen_evaluacion TO role_coordinador, role_docente;
GRANT SELECT ON postulacion.evaluacion_meritos TO role_coordinador, role_docente;
GRANT SELECT ON postulacion.calificacion_oposicion_individual TO role_coordinador, role_docente;
GRANT SELECT ON postulacion.evaluacion_oposicion TO role_coordinador, role_docente;

-- Permisos sobre seguridad.usuario_comision
GRANT SELECT ON seguridad.usuario_comision TO role_coordinador, role_docente;
GRANT SELECT ON postulacion.comision_seleccion TO role_coordinador, role_docente;
