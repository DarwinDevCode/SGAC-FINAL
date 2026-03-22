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
    
    const res = await client.query(`
        SELECT
          pa.estado as periodo_estado,
          pa.activo as periodo_activo,
          c.activo as convocatoria_activa,
          p.activo as postulacion_activa,
          tea.codigo as estado_ayudantia
        FROM ayudantia.ayudantia a
        JOIN postulacion.postulacion p ON p.id_postulacion = a.id_postulacion
        JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.periodo_academico pa ON pa.id_periodo_academico = c.id_periodo_academico
        JOIN academico.docente d ON d.id_docente = c.id_docente
        JOIN academico.estudiante e ON e.id_estudiante = p.id_estudiante
        JOIN seguridad.usuario u ON u.id_usuario = e.id_usuario
        JOIN ayudantia.tipo_estado_ayudantia tea ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
        WHERE u.nombre_usuario = 'dninasuntar'
    `);
    
    console.log(JSON.stringify(res.rows, null, 2));

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
