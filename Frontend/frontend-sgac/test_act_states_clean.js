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
      SELECT id_tipo_estado_registro, codigo, nombre_estado FROM ayudantia.tipo_estado_registro ORDER BY id_tipo_estado_registro;
    `);
    
    idsQuery.rows.forEach(r => console.log(`${r.id_tipo_estado_registro}: ${r.codigo} - ${r.nombre_estado}`));

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
