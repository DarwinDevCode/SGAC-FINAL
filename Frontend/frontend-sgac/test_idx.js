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

    // Query unique indexes
    const idxQuery = await client.query(`
      SELECT indexname, indexdef
      FROM pg_indexes
      WHERE tablename = 'evidencia_registro_actividad';
    `);
    
    console.log("== Indexes en evidencia_registro_actividad ==");
    console.log(JSON.stringify(idxQuery.rows, null, 2));

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
