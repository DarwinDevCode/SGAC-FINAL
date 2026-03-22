
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

  const tablePermissions = await client.query(`
    SELECT grantee, table_schema, table_name, privilege_type
    FROM information_schema.role_table_grants
    WHERE grantee IN ('role_coordinador', 'administrador_consultas') 
      AND table_name = 'comision_seleccion'
  `);
  data.comisionPerms = tablePermissions.rows;

  const schemaUsage = await client.query(`
    SELECT nspname, 
           has_schema_privilege('administrador_consultas', nspname, 'USAGE') as admin_has_usage,
           has_schema_privilege('role_coordinador', nspname, 'USAGE') as coord_has_usage
    FROM pg_namespace
    WHERE nspname IN ('seguridad', 'academico', 'postulacion', 'convocatoria')
  `);
  data.schemaUsage = schemaUsage.rows;

  fs.writeFileSync('admin_perms_check.json', JSON.stringify(data, null, 2));
  console.log('Results written to admin_perms_check.json');

  await client.end();
}

run().catch(console.error);
