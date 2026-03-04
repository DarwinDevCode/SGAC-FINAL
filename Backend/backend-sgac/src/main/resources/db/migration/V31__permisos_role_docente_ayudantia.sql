-- V31: Permisos del rol role_docente sobre el schema ayudantia
-- El DatabaseRoleAspect cambia la sesión a role_docente para endpoints del docente,
-- pero ese rol no tenía acceso al schema ayudantia ni a sus tablas/funciones.

-- Acceso al schema
GRANT USAGE ON SCHEMA ayudantia TO role_docente;

-- Acceso de lectura a todas las tablas del schema
GRANT SELECT ON ALL TABLES IN SCHEMA ayudantia TO role_docente;

-- Ejecutar stored procedures del docente (en schema public)
GRANT EXECUTE ON FUNCTION public.sp_resumen_docente(INTEGER)              TO role_docente;
GRANT EXECUTE ON FUNCTION public.sp_listar_ayudantes_docente(INTEGER)     TO role_docente;
GRANT EXECUTE ON FUNCTION public.sp_actividades_ayudante_docente(INTEGER) TO role_docente;
GRANT EXECUTE ON FUNCTION public.sp_evidencias_actividad_docente(INTEGER) TO role_docente;

-- Permisos de escritura para cambiar estado de actividades y evidencias
GRANT UPDATE ON ayudantia.registro_actividad                TO role_docente;
GRANT UPDATE ON ayudantia.evidencia_registro_actividad      TO role_docente;
