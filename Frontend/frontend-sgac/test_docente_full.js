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
    
    const schemas = ['postulacion', 'academico', 'convocatoria', 'seguridad', 'public', 'oposicion'];

    for (let schema of schemas) {
      console.log(`Granting to role_docente on schema ${schema}...`);
      await client.query(`GRANT USAGE ON SCHEMA ${schema} TO role_docente;`);
      await client.query(`GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ${schema} TO role_docente;`);
      await client.query(`GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema} TO role_docente;`);
      
      // Also grant on sequences for INSERTs
      await client.query(`GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ${schema} TO role_docente;`);
    }

    console.log("Full DB permissions for role_docente have been granted successfully.");

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
