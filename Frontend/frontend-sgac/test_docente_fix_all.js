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
    
    const schemas = ['ayudantia', 'postulacion', 'academico', 'convocatoria', 'seguridad', 'public'];
    
    for (const schema of schemas) {
      await client.query(`GRANT USAGE ON SCHEMA ${schema} TO role_docente;`);
      await client.query(`GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ${schema} TO role_docente;`);
      await client.query(`GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ${schema} TO role_docente;`);
      await client.query(`GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema} TO role_docente;`);
    }
    console.log("Grants aplicados exitosamente a todos los esquemas necesarios.");

  } catch (err) {
    console.error("Error al aplicar grants:", err.message);
  } finally {
    await client.end();
  }
}

run();
