
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
  
  const result = await client.query(`
    SELECT n.nspname as schema, p.proname as function, pg_get_userbyid(p.proowner) as owner, p.prosecdef as is_security_definer
    FROM pg_proc p
    JOIN pg_namespace n ON p.pronamespace = n.oid
    WHERE p.proname = 'fn_consultar_cronograma_oposicion'
  `);

  fs.writeFileSync('cronograma_owner.json', JSON.stringify(result.rows, null, 2));
  console.log('Results written to cronograma_owner.json');

  await client.end();
}

run().catch(console.error);
