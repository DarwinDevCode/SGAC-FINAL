-- Playbook: inspección y ajuste de permisos para creación de usuarios/roles desde SGAC
-- Ejecutar con un usuario con privilegios suficientes (idealmente el owner real de la BD o superusuario).

-- =========================================================
-- 1) INSPECCIÓN: ¿Qué permisos tiene role_administrador?
-- =========================================================
SELECT
  r.rolname,
  r.rolsuper,
  r.rolinherit,
  r.rolcreaterole,
  r.rolcreatedb,
  r.rolcanlogin,
  r.rolreplication
FROM pg_roles r
WHERE r.rolname = 'role_administrador';

-- Membresías directas del rol (qué roles tiene asignados)
SELECT
  child.rolname  AS member,
  parent.rolname AS granted_role
FROM pg_auth_members m
JOIN pg_roles parent ON parent.oid = m.roleid
JOIN pg_roles child  ON child.oid  = m.member
WHERE child.rolname = 'role_administrador';

-- ¿Quiénes tienen role_administrador?
SELECT
  child.rolname  AS member,
  parent.rolname AS granted_role
FROM pg_auth_members m
JOIN pg_roles parent ON parent.oid = m.roleid
JOIN pg_roles child  ON child.oid  = m.member
WHERE parent.rolname = 'role_administrador';

-- =========================================================
-- 2) AJUSTE RECOMENDADO PARA TU CASO
-- =========================================================
-- NOTA CLAVE:
-- En PostgreSQL, atributos de rol como CREATEROLE/CREATEDB/SUPERUSER NO se heredan
-- por membresía. Si `admin1` tiene `role_administrador` pero el CURRENT_USER es `admin1`,
-- CREATE USER puede fallar. Debe hacerse SET ROLE role_administrador antes del CREATE USER.

-- Si los usuarios administrativos deben poder ejecutar CREATE USER/ROLE,
-- role_administrador necesita CREATEROLE.
ALTER ROLE role_administrador CREATEROLE;

-- Recomendado: heredar privilegios de los roles concedidos.
ALTER ROLE role_administrador INHERIT;

-- Si el error es "permission denied to grant role "role_docente"" (o similar),
-- al rol activo le falta ADMIN OPTION sobre el rol que intenta asignar.
-- Ejemplos:
GRANT role_docente TO role_administrador WITH ADMIN OPTION;
GRANT role_estudiante TO role_administrador WITH ADMIN OPTION;
GRANT role_coordinador TO role_administrador WITH ADMIN OPTION;
GRANT role_decano TO role_administrador WITH ADMIN OPTION;
GRANT role_ayudante_catedra TO role_administrador WITH ADMIN OPTION;


-- Diagnóstico específico para "permission denied to grant role "role_administrador"":
-- verificar quién tiene ADMIN OPTION sobre role_administrador.
SELECT
  parent.rolname AS rol_objetivo,
  child.rolname  AS otorgado_a,
  m.admin_option,
  grantor.rolname AS otorgado_por
FROM pg_auth_members m
JOIN pg_roles parent  ON parent.oid  = m.roleid
JOIN pg_roles child   ON child.oid   = m.member
LEFT JOIN pg_roles grantor ON grantor.oid = m.grantor
WHERE parent.rolname = 'role_administrador'
ORDER BY child.rolname;

-- Si el SP se ejecuta con SET ROLE role_administrador, ese rol debe poder otorgarse.
-- Ejecutar con un superusuario/owner:
GRANT role_administrador TO role_administrador WITH ADMIN OPTION;

-- Si usas SECURITY DEFINER y owner = admin1, también conviene:
GRANT role_administrador TO admin1 WITH ADMIN OPTION;

-- (Opcional) Si app_user_default debe poder SET ROLE a usuarios creados dinámicamente,
-- tu SP ya hace: GRANT <usuario_creado> TO app_user_default.
-- Verificación rápida:
SELECT
  child.rolname  AS member,
  parent.rolname AS granted_role
FROM pg_auth_members m
JOIN pg_roles parent ON parent.oid = m.roleid
JOIN pg_roles child  ON child.oid  = m.member
WHERE child.rolname = 'app_user_default'
ORDER BY parent.rolname;

-- =========================================================
-- 3) PROCEDIMIENTO: endurecer seguridad y ejecución con owner
-- =========================================================
-- IMPORTANTE:
-- Para evitar depender de privilegios del invocador en operaciones CREATE USER,
-- define el procedimiento como SECURITY DEFINER y con search_path fijo.

ALTER PROCEDURE public.sp_registrar_administrador(
  character varying,
  character varying,
  character varying,
  character varying,
  character varying,
  character varying
) SECURITY DEFINER;

ALTER PROCEDURE public.sp_registrar_administrador(
  character varying,
  character varying,
  character varying,
  character varying,
  character varying,
  character varying
) SET search_path = public, pg_temp;

-- Recomendado: NO dejar EXECUTE a PUBLIC en procedimientos sensibles.
REVOKE EXECUTE ON PROCEDURE public.sp_registrar_administrador(
  character varying,
  character varying,
  character varying,
  character varying,
  character varying,
  character varying
) FROM PUBLIC;

GRANT EXECUTE ON PROCEDURE public.sp_registrar_administrador(
  character varying,
  character varying,
  character varying,
  character varying,
  character varying,
  character varying
) TO rol_sgac_app;

GRANT EXECUTE ON PROCEDURE public.sp_registrar_administrador(
  character varying,
  character varying,
  character varying,
  character varying,
  character varying,
  character varying
) TO role_administrador;

-- =========================================================
-- 4) VALIDACIÓN POSTERIOR
-- =========================================================
-- Confirma flags finales:
SELECT rolname, rolinherit, rolcreaterole
FROM pg_roles
WHERE rolname IN ('role_administrador', 'app_user_default');
