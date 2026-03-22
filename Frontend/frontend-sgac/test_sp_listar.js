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
    
    // Check sp_listar_ayudantes_docente source
    const res = await client.query(`
      SELECT pg_get_functiondef(p.oid) as def
      FROM pg_proc p 
      JOIN pg_namespace n ON p.pronamespace = n.oid 
      WHERE p.proname = 'sp_listar_ayudantes_docente'
    `);
    
    console.log("== SP_LISTAR_AYUDANTES_DOCENTE ==");
    if(res.rows.length > 0) {
      console.log(res.rows[0].def);
    }
    
    // Also test it as role_docente
    try {
      await client.query("SET ROLE role_docente;");
      
      const teacherRes = await client.query("SELECT id_usuario FROM seguridad.usuario WHERE nombre_usuario = 'ediazm';");
      const idUsuario = teacherRes.rows[0].id_usuario;
      console.log(`\nTeacher idUsuario 'ediazm': ${idUsuario}`);
      
      const testCmd = await client.query(`SELECT * FROM public.sp_listar_ayudantes_docente(${idUsuario});`);
      console.log(`\nResultados sp_listar_ayudantes_docente: ${testCmd.rows.length}`);
      
    } catch(err) {
      console.error("\nError probando como role_docente:", err.message);
    }
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
