-- V28: Solución integral de permisos para asignar comisión de evaluación
-- Se re-asignan y aseguran todos los permisos necesarios para el rol coordinador
-- independientemente de si los fallos fueron de esquema o de la migración V26.

-- 1. Permisos de Ejecución sobre Procedimientos Almacenados
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO role_coordinador;
GRANT EXECUTE ON FUNCTION public.sp_crear_usuario_comision(INTEGER, INTEGER, INTEGER, VARCHAR, NUMERIC, NUMERIC, NUMERIC, DATE) TO role_coordinador;

-- Aseguramos que la aplicación web predeterminada también tenga acceso si el pool usa app_user_default
GRANT EXECUTE ON FUNCTION public.sp_crear_evaluacion_oposicion(INTEGER, VARCHAR, DATE, TIME, TIME, VARCHAR, VARCHAR) TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_crear_usuario_comision(INTEGER, INTEGER, INTEGER, VARCHAR, NUMERIC, NUMERIC, NUMERIC, DATE) TO app_user_default;

-- 2. Permisos DML (Insertar, Actualizar, Leer) sobre Tablas
GRANT INSERT, UPDATE, SELECT, DELETE ON TABLE postulacion.evaluacion_oposicion TO role_coordinador;
GRANT INSERT, UPDATE, SELECT, DELETE ON TABLE seguridad.usuario_comision TO role_coordinador;
GRANT SELECT ON TABLE postulacion.comision_seleccion TO role_coordinador;
GRANT SELECT ON TABLE postulacion.postulacion TO role_coordinador;
GRANT SELECT ON TABLE seguridad.usuario TO role_coordinador;

-- 3. Permisos sobre Secuencias (Auto-incrementables)
GRANT USAGE, SELECT ON SEQUENCE postulacion.evaluacion_oposicion_id_evaluacion_oposicion_seq TO role_coordinador;
GRANT USAGE, SELECT ON SEQUENCE seguridad.usuario_comision_id_usuario_comision_seq TO role_coordinador;

-- 4. Permisos Extras y de Esquema (por precaución)
GRANT USAGE ON SCHEMA postulacion TO role_coordinador;
GRANT USAGE ON SCHEMA seguridad TO role_coordinador;
GRANT USAGE ON SCHEMA public TO role_coordinador;
