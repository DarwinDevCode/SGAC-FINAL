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
    
    const schemas = ['ayudantia', 'postulacion', 'academico', 'convocatoria', 'notificacion', 'seguridad', 'public'];
    const roles = ['role_estudiante', 'role_ayudante_catedra'];

    for (let role of roles) {
      console.log(`Granting to ${role}...`);
      for (let schema of schemas) {
        await client.query(`GRANT USAGE ON SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ${schema} TO ${role};`);
      }
    }

    console.log("Full DB permissions for estudiantes have been granted successfully.");

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
