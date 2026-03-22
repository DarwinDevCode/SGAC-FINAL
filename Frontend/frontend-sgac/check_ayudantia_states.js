const { Client } = require('pg');
const fs = require('fs');

const client = new Client({
  connectionString: "postgresql://postgres:Postgresql123.@localhost:5432/SGAC-FINAL"
});

async function checkStates() {
  try {
    await client.connect();
    
    const res = await client.query(`
        SELECT column_name, is_nullable, column_default
        FROM information_schema.columns
        WHERE table_schema = 'ayudantia' AND table_name = 'registro_actividad'
    `);
    fs.writeFileSync('table_def.txt', JSON.stringify(res.rows, null, 2));
    console.log("Definición escrita en table_def.txt");

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

checkStates();
