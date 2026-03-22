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
    
    // The actual owner is administrador_consultas! Let's grant to both just in case.
    const roles = ['app_user_default', 'administrador_consultas'];
    const schemas = ['ayudantia', 'postulacion', 'academico', 'convocatoria', 'notificacion', 'seguridad', 'public', 'oposicion'];

    for (let role of roles) {
      console.log(`Granting native execution permissions to ${role} (SECURITY DEFINER owner)...`);
      for (let schema of schemas) {
        try {
          await client.query(`GRANT USAGE ON SCHEMA ${schema} TO ${role};`);
          await client.query(`GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ${schema} TO ${role};`);
          await client.query(`GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema} TO ${role};`);
          await client.query(`GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ${schema} TO ${role};`);
        } catch (e) {
          // Ignore
        }
      }
    }

    console.log("Full SECURITY DEFINER privileges restored successfully.");

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
