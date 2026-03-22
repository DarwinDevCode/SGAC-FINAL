
const { Client } = require('pg');

const client = new Client({
  user: 'postgres',
  host: 'localhost',
  database: 'SGAC-FINAL',
  password: 'Postgresql123.',
  port: 5432,
});

async function run() {
  await client.connect();
  
  // Get an id_convocatoria to test
  const conv = await client.query('SELECT id_convocatoria FROM convocatoria.convocatoria LIMIT 1');
  if (conv.rows.length === 0) {
    console.log('No convocatorias found');
    await client.end();
    return;
  }
  const id = conv.rows[0].id_convocatoria;
  console.log(`Testing with id_convocatoria: ${id}`);

  try {
    console.log('--- Executing as role_coordinador ---');
    await client.query('BEGIN');
    await client.query('SET LOCAL ROLE role_coordinador');
    const result = await client.query('SELECT CAST(postulacion.fn_consultar_cronograma_oposicion($1) AS text)', [id]);
    console.log('Result:', JSON.parse(result.rows[0].fn_consultar_cronograma_oposicion));
    await client.query('ROLLBACK');
  } catch (err) {
    console.error('Error executing as role_coordinador:', err.message);
    if (err.detail) console.error('Detail:', err.detail);
    if (err.hint) console.error('Hint:', err.hint);
    if (err.where) console.error('Where:', err.where);
    await client.query('ROLLBACK');
  }

  await client.end();
}

run().catch(console.error);
