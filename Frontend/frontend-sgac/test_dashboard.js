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
    
    // Add notificacion
    await client.query("GRANT USAGE ON SCHEMA notificacion TO role_docente;");
    await client.query("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA notificacion TO role_docente;");
    await client.query("GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA notificacion TO role_docente;");
    await client.query("GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA notificacion TO role_docente;");
    console.log("Grants para notificacion aplicados.");
    
    await client.query("SET ROLE role_docente;");
    
    const teacherRes = await client.query("SELECT id_usuario FROM seguridad.usuario WHERE nombre_usuario = 'ediazm';");
    const idUsuario = teacherRes.rows[0].id_usuario;
    
    // Probar sp_resumen_docente
    try {
      const dbRes = await client.query(`SELECT * FROM public.sp_resumen_docente(${idUsuario});`);
      console.log(`sp_resumen_docente exitoso: ${dbRes.rows.length}`);
    } catch(e) {
      console.error("sp_resumen_docente Error:", e.message);
    }

    // Probar tabla notificaciones
    try {
      const dbRes = await client.query(`SELECT * FROM notificacion.notificacion LIMIT 1;`);
      console.log(`notificacion exitoso`);
    } catch(e) {
      console.error("notificacion Error:", e.message);
    }
    
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
