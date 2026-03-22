
const { Client } = require('pg');
const fs = require('fs');

const client = new Client({
  user: 'postgres',
  host: 'localhost',
  database: 'SGAC-FINAL',
  password: 'Postgresql123.',
  port: 5432,
});

async function run() {
  await client.connect();
  
  const tables = [
    { s: 'postulacion', t: 'postulacion' },
    { s: 'postulacion', t: 'evaluacion_oposicion' },
    { s: 'postulacion', t: 'comision_seleccion' },
    { s: 'postulacion', t: 'tipo_estado_evaluacion' },
    { s: 'postulacion', t: 'tipo_estado_postulacion' },
    { s: 'academico', t: 'estudiante' },
    { s: 'academico', t: 'coordinador' },
    { s: 'academico', t: 'asignatura' },
    { s: 'academico', t: 'carrera' },
    { s: 'convocatoria', t: 'convocatoria' },
    { s: 'convocatoria', t: 'tipo_estado_requisito' },
    { s: 'seguridad', t: 'usuario' },
    { s: 'seguridad', t: 'usuario_comision' }
  ];

  const results = [];

  for (const tab of tables) {
    const coord_sel = await client.query(`SELECT has_table_privilege('role_coordinador', '${tab.s}.${tab.t}', 'SELECT')`);
    const admin_sel = await client.query(`SELECT has_table_privilege('administrador_consultas', '${tab.s}.${tab.t}', 'SELECT')`);
    
    results.push({
      table: `${tab.s}.${tab.t}`,
      coordinator_select: coord_sel.rows[0].has_table_privilege,
      admin_select: admin_sel.rows[0].has_table_privilege
    });
  }

  fs.writeFileSync('comprehensive_perms_check.json', JSON.stringify(results, null, 2));
  console.log('Results written to comprehensive_perms_check.json');

  await client.end();
}

run().catch(console.error);
