
const { Client } = require('pg');
const fs = require('fs');

const client = new Client({
  user: 'postgres',
  host: 'localhost',
  database: 'SGAC-FINAL',
  password: 'Postgresql123.',
  port: 5432,
});

async function run() {
  await client.connect();
  
  const data = {};

  const roles = await client.query("SELECT rolname FROM pg_roles WHERE rolname IN ('role_coordinador', 'administrador_consultas')");
  data.roles = roles.rows.map(r => r.rolname);

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
  data.functions = functions.rows;

  const schemaPerms = await client.query(`
    SELECT nspname, 
           has_schema_privilege('role_coordinador', nspname, 'USAGE') as has_usage
    FROM pg_namespace
    WHERE nspname IN ('academico', 'postulacion', 'convocatoria', 'ayudantia', 'seguridad', 'public')
  `);
  data.schemaPerms = schemaPerms.rows;

  const tablePermissions = await client.query(`
    SELECT table_schema, table_name, privilege_type
    FROM information_schema.role_table_grants
    WHERE grantee = 'role_coordinador' AND table_schema IN ('academico', 'postulacion', 'convocatoria')
  `);
  data.tablePermissions = tablePermissions.rows;

  fs.writeFileSync('inspect_result.json', JSON.stringify(data, null, 2));
  console.log('Results written to inspect_result.json');

  await client.end();
}

run().catch(console.error);
