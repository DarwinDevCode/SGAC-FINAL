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

    // Set role
    await client.query("SET LOCAL ROLE role_ayudante_catedra;");

    console.log("Testing fn_progreso_general...");
    const progreso = await client.query(`
      SELECT * FROM ayudantia.fn_progreso_general(152);
    `);
    console.log(progreso.rows);

  } catch (err) {
    console.error("Error running function:", err.message);
  } finally {
    await client.end();
  }
}

run();
