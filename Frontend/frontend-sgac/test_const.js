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

    // Consultar constrains
    const constQuery = await client.query(`
      SELECT conname, pg_get_constraintdef(c.oid)
      FROM pg_constraint c
      JOIN pg_namespace n ON n.oid = c.connamespace
      WHERE conrelid = 'ayudantia.evidencia_registro_actividad'::regclass;
    `);
    
    console.log("== Constraints en evidencia_registro_actividad ==");
    console.log(JSON.stringify(constQuery.rows, null, 2));

    // Try a dummy update to replicate the error
    try {
        const idEvidencia = 1; // Assuming 1 exists, or we query one
        const oneEv = await client.query("SELECT id_evidencia_registro_actividad FROM ayudantia.evidencia_registro_actividad LIMIT 1;");
        if(oneEv.rows.length > 0) {
            const id = oneEv.rows[0].id_evidencia_registro_actividad;
            console.log("Trying to update ID:", id);
            await client.query(`
                UPDATE ayudantia.evidencia_registro_actividad
                SET observaciones = 'test', fecha_observacion = CURRENT_DATE
                WHERE id_evidencia_registro_actividad = $1
            `, [id]);
            console.log("Update OK");
        } else {
            console.log("No evidences found to test update");
        }
    } catch(err) {
        console.error("Update error:", err.message);
    }
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
