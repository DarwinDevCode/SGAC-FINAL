
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
    WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
  `);

  fs.writeFileSync('all_functions_inventory.json', JSON.stringify(result.rows, null, 2));
  console.log('Results written to all_functions_inventory.json');

  await client.end();
}

run().catch(console.error);
