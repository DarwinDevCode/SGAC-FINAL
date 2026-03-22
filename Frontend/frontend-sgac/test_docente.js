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
    
    await client.query("SET ROLE role_docente;");
    
    // Probar acceso al esquema ayudantia
    try {
      await client.query("SELECT * FROM ayudantia.ayudantia LIMIT 1;");
      console.log("SELECT en ayudantia exitoso.");
    } catch(err) {
      console.error("Error SELECT ayudantia:", err.message);
    }
    
    // Probar SPs de docente
    try {
      await client.query("SELECT * FROM public.sp_listar_ayudantes_docente(1)");
      console.log("Exitoso sp_listar_ayudantes_docente");
    } catch(err) {
      console.error("Error sp_listar:", err.message);
    }

  } catch (err) {
    console.error("Error principal:", err.message);
  } finally {
    await client.end();
  }
}

run();
