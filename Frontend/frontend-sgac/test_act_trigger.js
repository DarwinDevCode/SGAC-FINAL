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

    // Check triggers on registro_actividad
    const triggerQuery = await client.query(`
      SELECT trigger_name, event_manipulation, event_object_table, action_statement
      FROM information_schema.triggers
      WHERE event_object_table = 'registro_actividad';
    `);
    
    console.log("== Triggers en registro_actividad ==");
    console.log(JSON.stringify(triggerQuery.rows, null, 2));

    for(let row of triggerQuery.rows) {
        const match = row.action_statement.match(/EXECUTE FUNCTION ([\w.]+)\(/);
        if (match) {
            const funcName = match[1];
            const funcQuery = await client.query(`
                SELECT pg_get_functiondef(p.oid) as def
                FROM pg_proc p 
                JOIN pg_namespace n ON p.pronamespace = n.oid 
                WHERE n.nspname || '.' || p.proname = '${funcName}'
                   OR p.proname = '${funcName}'
            `);
            console.log("Function Definition:", funcQuery.rows[0]?.def);
        }
    }

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
