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

    // The roles that might be used by the student/assistant
    const roles = ['role_estudiante', 'role_ayudante_catedra'];
    const schemas = ['ayudantia', 'postulacion', 'academico', 'convocatoria', 'notificacion', 'seguridad', 'public'];

    for (const role of roles) {
      // Check if role exists before granting
      const roleRes = await client.query(`SELECT 1 FROM pg_roles WHERE rolname = $1`, [role]);
      if (roleRes.rows.length === 0) {
        console.log(`Role ${role} does not exist, skipping...`);
        continue;
      }

      console.log(`Granting to ${role}...`);
      for (const schema of schemas) {
        await client.query(`GRANT USAGE ON SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ${schema} TO ${role};`);
        await client.query(`GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema} TO ${role};`);
      }
    }
    
    console.log("Grants estudiantes aplicados exitosamente.");

  } catch (err) {
    console.error("Error al aplicar grants:", err.message);
  } finally {
    await client.end();
  }
}

run();
