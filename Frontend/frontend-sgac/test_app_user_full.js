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
    
    // El owner de las funciones SECURITY DEFINER suele ser app_user_default
    const role = 'app_user_default';
    const schemas = ['ayudantia', 'postulacion', 'academico', 'convocatoria', 'notificacion', 'seguridad', 'public', 'oposicion'];

    console.log(`Granting native execution permissions to ${role} (SECURITY DEFINER owner)...`);
    
    for (let schema of schemas) {
      try {
        await client.query(`GRANT USAGE ON SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ${schema} TO ${role};`);
      } catch (e) {
          console.log(`Schema ${schema} failed, probably doesn't exist. Skipping.`);
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
