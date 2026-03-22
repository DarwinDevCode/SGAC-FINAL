
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
    SELECT n.nspname, p.proname 
    FROM pg_proc p 
    JOIN pg_namespace n ON p.pronamespace = n.oid 
    WHERE p.proname = 'fn_obtener_estadisticas_decano'
  `);

  fs.writeFileSync('decano_stats_search.json', JSON.stringify(result.rows, null, 2));
  console.log('Results written to decano_stats_search.json');

  await client.end();
}

run().catch(console.error);
