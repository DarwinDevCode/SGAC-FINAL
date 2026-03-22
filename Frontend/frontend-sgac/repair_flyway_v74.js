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
    console.log("Connected to database to repair Flyway checksum...");

    // Find the history table. It usually depends on the first schema in flyway.schemas
    // In application.properties: spring.flyway.schemas=seguridad, public, ...
    const checksum = 1494510010;
    const version = '74';

    const tables = ['seguridad.flyway_schema_history', 'public.flyway_schema_history'];
    
    for (let table of tables) {
        try {
            const res = await client.query(`UPDATE ${table} SET checksum = $1 WHERE version = $2`, [checksum, version]);
            if (res.rowCount > 0) {
                console.log(`Successfully updated checksum for version ${version} in ${table}`);
                break;
            }
        } catch (e) {
            console.log(`Table ${table} not found or error: ${e.message}`);
        }
    }

    console.log("Flyway repair attempt finished.");

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
