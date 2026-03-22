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
    console.log("Connected to DB.");
    const res = await client.query(`
      SELECT p.proname, pg_get_functiondef(p.oid) as def
      FROM pg_proc p 
      JOIN pg_namespace n ON p.pronamespace = n.oid 
      WHERE n.nspname = 'ayudantia' 
      AND p.proname IN ('fn_obtener_asistencia_sesion_actual', 'fn_planificar_sesion')
    `);
    
    for (let row of res.rows) {
      console.log('--- FUNCTION: ' + row.proname + ' ---');
      console.log(row.def);
    }
  } catch (err) {
    console.error("Error executing query:", err);
  } finally {
    await client.end();
  }
}

run();
