
const { Client } = require('pg');

const client = new Client({
  user: 'app_user_default',
  host: 'localhost',
  database: 'sgac_final',
  password: 'app_user_default',
  port: 5432,
});

async function run() {
  await client.connect();
  console.log('--- Inspecting Roles ---');
  const roles = await client.query("SELECT rolname FROM pg_roles WHERE rolname IN ('role_coordinador', 'administrador_consultas')");
  console.log('Roles found:', roles.rows.map(r => r.rolname));

  console.log('\n--- Inspecting Function Owners ---');
  const functions = await client.query(`
    SELECT n.nspname as schema, p.proname as function, pg_get_userbyid(p.proowner) as owner, p.prosecdef as is_security_definer
    FROM pg_proc p
    JOIN pg_namespace n ON p.pronamespace = n.oid
    WHERE p.proname IN (
      'fn_obtener_estadisticas_coordinador',
      'fn_reporte_convocatorias_coordinador',
      'fn_reporte_postulantes_coordinador',
      'fn_listar_postulaciones_coordinador',
      'fn_obtener_detalle_postulacion_coordinador'
    )
  `);
  console.table(functions.rows);

  console.log('\n--- Inspecting Permissions for role_coordinador ---');
  const permissions = await client.query(`
    SELECT nspname, privilege_type
    FROM information_schema.usage_privileges
    WHERE grantee = 'role_coordinador' AND nspname IN ('academico', 'postulacion')
  `);
  console.log('Usage privileges for role_coordinador:', permissions.rows);

  const tablePermissions = await client.query(`
    SELECT table_schema, table_name, privilege_type
    FROM information_schema.role_table_grants
    WHERE grantee = 'role_coordinador' AND table_schema IN ('academico', 'postulacion')
  `);
  console.log('Table privileges for role_coordinador (first 10):', tablePermissions.rows.slice(0, 10));

  await client.end();
}

run().catch(console.error);
