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

    const idsQuery = await client.query(`
      SELECT * FROM ayudantia.tipo_estado_evidencia;
    `);
    
    console.log("== tipo_estado_evidencia ==");
    console.log(JSON.stringify(idsQuery.rows, null, 2));

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
