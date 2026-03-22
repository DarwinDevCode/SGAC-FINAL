
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
    SELECT pg_get_functiondef(p.oid) as definition
    FROM pg_proc p
    JOIN pg_namespace n ON p.pronamespace = n.oid
    WHERE p.proname = 'fn_obtener_estadisticas_coordinador'
  `);

  if (result.rows.length > 0) {
    fs.writeFileSync('stats_function_def.sql', result.rows[0].definition);
    console.log('Definition written to stats_function_def.sql');
  } else {
    // Try in another schema or look for all functions
    const all = await client.query(`SELECT n.nspname, p.proname FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE p.proname LIKE '%estadisticas_coordinador%'`);
    console.log('Found stats functions:', all.rows);
  }

  await client.end();
}

run().catch(console.error);
