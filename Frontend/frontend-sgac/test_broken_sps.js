const { Client } = require('pg');

const client = new Client({
  user: 'app_user_default',
  host: 'localhost',
  database: 'SGAC-FINAL',
  password: 'AppUser123.', // Let's try this or just use postgres and SET ROLE
  port: 5432,
});

async function run() {
  const adminClient = new Client({
    user: 'postgres',
    host: 'localhost',
    database: 'SGAC-FINAL',
    password: 'Postgresql123.',
    port: 5432,
  });

  try {
    await adminClient.connect();
    console.log("Connected as postgres");

    // Let's see who owns the function fn_consultar_cronograma_oposicion
    let res = await adminClient.query(`
        SELECT proowner::regrole 
        FROM pg_proc 
        WHERE proname = 'fn_consultar_cronograma_oposicion';
    `);
    console.log("Function Owner: ", res.rows);

    // Let's see who owns the table comision_seleccion
    res = await adminClient.query(`
        SELECT tableowner 
        FROM pg_tables 
        WHERE tablename = 'comision_seleccion';
    `);
    console.log("Table Owner: ", res.rows);

    // Let's test if we can SET ROLE to role_docente and select from it
    console.log("Testing as postgres with SET ROLE role_docente...");
    await adminClient.query("SET ROLE role_docente;");
    
    try {
        let test1 = await adminClient.query('SELECT * FROM postulacion.comision_seleccion LIMIT 1;');
        console.log("Success! role_docente CAN select from comision_seleccion! rows: " + test1.rowCount);
    } catch(e) {
        console.log("FAILED! role_docente cannot select: " + e.message);
    }

    await adminClient.query("RESET ROLE;");

    // Let's test if app_user_default can select from it
    console.log("Testing as postgres with SET ROLE app_user_default...");
    await adminClient.query("SET ROLE app_user_default;");
    try {
        let test2 = await adminClient.query('SELECT * FROM postulacion.comision_seleccion LIMIT 1;');
        console.log("Success! app_user_default CAN select from comision_seleccion! rows: " + test2.rowCount);
    } catch(e) {
        console.log("FAILED! app_user_default cannot select: " + e.message);
    }
    
    await adminClient.query("RESET ROLE;");

    // Let's check exactly what the function does that fails
    console.log("Executing fn_consultar_cronograma_oposicion as role_docente...");
    await adminClient.query("SET ROLE role_docente;");
    try {
        let test3 = await adminClient.query('SELECT postulacion.fn_consultar_cronograma_oposicion(57);');
        console.log("Success! fn_consultar_cronograma_oposicion returned: ", test3.rows[0]);
    } catch(e) {
        console.log("FAILED to execute function: " + e.message);
    }

  } catch (err) {
    console.error(err);
  } finally {
    await adminClient.end();
  }
}

run();
