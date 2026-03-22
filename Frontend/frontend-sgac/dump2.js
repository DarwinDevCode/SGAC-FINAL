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
    
    // Get doc info
    const res = await client.query(`
      SELECT asig.nombre_asignatura, doc_usu.nombres || ' ' || doc_usu.apellidos AS nombre_docente, doc_usu.correo, est_usu.nombres as ayud_nombre, est_usu.username
      FROM academico.estudiante e
      JOIN seguridad.usuario est_usu ON e.id_usuario = est_usu.id_usuario
      JOIN postulacion.postulacion p ON p.id_estudiante = e.id_estudiante
      JOIN ayudantia.ayudantia a ON a.id_postulacion = p.id_postulacion
      JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
      JOIN academico.docente d ON d.id_docente = c.id_docente
      JOIN seguridad.usuario doc_usu ON doc_usu.id_usuario = d.id_usuario
      JOIN academico.asignatura asig ON asig.id_asignatura = c.id_asignatura
      WHERE a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
      LIMIT 5;
    `);
    
    console.log(JSON.stringify(res.rows, null, 2));
  } catch (err) {
    // maybe column username is error, try nombre_usuario
    try {
      const res2 = await client.query(`
        SELECT asig.nombre_asignatura, doc_usu.nombres || ' ' || doc_usu.apellidos AS nombre_docente, doc_usu.correo, est_usu.nombres as ayud_nombre, est_usu.nombre_usuario as username
        FROM academico.estudiante e
        JOIN seguridad.usuario est_usu ON e.id_usuario = est_usu.id_usuario
        JOIN postulacion.postulacion p ON p.id_estudiante = e.id_estudiante
        JOIN ayudantia.ayudantia a ON a.id_postulacion = p.id_postulacion
        JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.docente d ON d.id_docente = c.id_docente
        JOIN seguridad.usuario doc_usu ON doc_usu.id_usuario = d.id_usuario
        JOIN academico.asignatura asig ON asig.id_asignatura = c.id_asignatura
        WHERE a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
        LIMIT 5;
      `);
      console.log(JSON.stringify(res2.rows, null, 2));
    } catch(err2) {
      console.error(err2);
    }
  } finally {
    await client.end();
  }
}

run();
