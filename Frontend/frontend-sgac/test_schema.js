const { Client } = require('pg');

const client = new Client({
  user: 'postgres',
  host: 'localhost',
  database: 'SGAC-FINAL',
  password: 'Postgresql123.',
  port: 5432,
});

async function run() {
  try {
    await client.connect();
    const res = await client.query("SELECT table_schema FROM information_schema.tables WHERE table_name = 'comision_seleccion';");
    console.log("Schema:", res.rows[0]?.table_schema);
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
