
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
    WHERE p.proname = 'fn_consultar_cronograma_oposicion'
  `);

  if (result.rows.length > 0) {
    fs.writeFileSync('cronograma_function_def.sql', result.rows[0].definition);
    console.log('Definition written to cronograma_function_def.sql');
  } else {
    console.log('Function not found');
  }

  await client.end();
}

run().catch(console.error);
