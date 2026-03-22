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
    
    // Apply grants as superuser
    await client.query("GRANT USAGE ON SCHEMA ayudantia TO role_docente;");
    await client.query("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ayudantia TO role_docente;");
    await client.query("GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ayudantia TO role_docente;");
    await client.query("GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ayudantia TO role_docente;");
    console.log("Grants aplicados exitosamente.");

  } catch (err) {
    console.error("Error al aplicar grants:", err.message);
  } finally {
    await client.end();
  }
}

run();
