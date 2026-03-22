package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Ayudantia;

import java.util.List;

@Repository
public interface DocenteMisAyudantesRepository extends JpaRepository<Ayudantia, Integer> {

    /**
     * Lista ayudantías del docente autenticado SOLO del período EN PROCESO.
     *
     * Cruce: docente -> convocatoria -> postulacion -> ayudantia -> estudiante(usuario)
     */
    @Query(value = """
        SELECT
          a.id_ayudantia,
          c.id_convocatoria,
          pa.id_periodo_academico,
          pa.nombre_periodo,
          asi.id_asignatura,
          asi.nombre_asignatura,
          u.id_usuario          AS id_usuario_ayudante,
          u.nombres,
          u.apellidos,
          a.fecha_inicio,
          a.fecha_fin,
          a.horas_maximas,
          a.horas_cumplidas,
          tea.nombre_estado     AS estado_ayudantia
        FROM ayudantia.ayudantia a
        JOIN postulacion.postulacion p
          ON p.id_postulacion = a.id_postulacion
        JOIN convocatoria.convocatoria c
          ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.periodo_academico pa
          ON pa.id_periodo_academico = c.id_periodo_academico
        JOIN academico.asignatura asi
          ON asi.id_asignatura = c.id_asignatura
        JOIN academico.docente d
          ON d.id_docente = c.id_docente
        JOIN academico.estudiante e
          ON e.id_estudiante = p.id_estudiante
        JOIN seguridad.usuario u
          ON u.id_usuario = e.id_usuario
        JOIN ayudantia.tipo_estado_ayudantia tea
          ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
        WHERE d.id_usuario = :idUsuario
          AND pa.activo = true
        ORDER BY a.id_ayudantia DESC
        """, nativeQuery = true)
    List<Object[]> listarMisAyudantes(@Param("idUsuario") Integer idUsuario);

    /**
     * Lista registros de actividad de una ayudantía validando que pertenece al docente (por idUsuario).
     */
    @Query(value = """
        SELECT
          ra.id_registro_actividad,
          ra.id_ayudantia,
          ra.descripcion_actividad,
          ra.tema_tratado,
          ra.fecha,
          (SELECT CAST(COUNT(*) AS INTEGER) FROM ayudantia.detalle_asistencia_actividad daa WHERE daa.id_registro_actividad = ra.id_registro_actividad) AS numero_asistentes,
          ra.horas_dedicadas,
          ra.id_tipo_estado_registro,
          ter.nombre_estado AS nombre_estado,
          ter.codigo AS codigo_estado,
          ra.observaciones,
          ra.fecha_observacion
        FROM ayudantia.registro_actividad ra
        JOIN ayudantia.tipo_estado_registro ter
          ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
        JOIN ayudantia.ayudantia a
          ON a.id_ayudantia = ra.id_ayudantia
        JOIN postulacion.postulacion p
          ON p.id_postulacion = a.id_postulacion
        JOIN convocatoria.convocatoria c
          ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.docente d
          ON d.id_docente = c.id_docente
        JOIN academico.periodo_academico pa
          ON pa.id_periodo_academico = c.id_periodo_academico
        WHERE ra.id_ayudantia = :idAyudantia
          AND d.id_usuario = :idUsuario
          AND pa.activo = true
        ORDER BY ra.fecha DESC, ra.id_registro_actividad DESC
        """, nativeQuery = true)
    List<Object[]> listarActividadesAyudantia(@Param("idUsuario") Integer idUsuario,
                                             @Param("idAyudantia") Integer idAyudantia);

    /**
     * Lista evidencias de un registro validando ownership por docente.
     */
    @Query(value = """
        SELECT
          ev.id_evidencia_registro_actividad,
          ev.nombre_archivo,
          ev.ruta_archivo,
          ev.mime_type,
          ev.tamanio_bytes,
          ev.fecha_subida,
          ev.id_tipo_estado_evidencia,
          tee.nombre_estado AS nombre_estado_evidencia,
          tee.codigo AS codigo_estado_evidencia,
          ev.observaciones,
          ev.fecha_observacion
        FROM ayudantia.evidencia_registro_actividad ev
        JOIN ayudantia.registro_actividad ra
          ON ra.id_registro_actividad = ev.id_registro_actividad
        JOIN ayudantia.ayudantia a
          ON a.id_ayudantia = ra.id_ayudantia
        JOIN postulacion.postulacion p
          ON p.id_postulacion = a.id_postulacion
        JOIN convocatoria.convocatoria c
          ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.docente d
          ON d.id_docente = c.id_docente
        JOIN academico.periodo_academico pa
          ON pa.id_periodo_academico = c.id_periodo_academico
        LEFT JOIN ayudantia.tipo_estado_evidencia tee
          ON tee.id_tipo_estado_evidencia = ev.id_tipo_estado_evidencia
        WHERE ev.id_registro_actividad = :idRegistroActividad
          AND d.id_usuario = :idUsuario
          AND pa.activo = true
        ORDER BY ev.id_evidencia_registro_actividad
        """, nativeQuery = true)
    List<Object[]> listarEvidenciasRegistro(@Param("idUsuario") Integer idUsuario,
                                           @Param("idRegistroActividad") Integer idRegistroActividad);

    /**
     * Devuelve el id_usuario del ayudante (estudiante.usuario) para una actividad validando ownership.
     */
    @Query(value = """
        SELECT u.id_usuario
        FROM ayudantia.registro_actividad ra
        JOIN ayudantia.ayudantia a
          ON a.id_ayudantia = ra.id_ayudantia
        JOIN postulacion.postulacion p
          ON p.id_postulacion = a.id_postulacion
        JOIN academico.estudiante e
          ON e.id_estudiante = p.id_estudiante
        JOIN seguridad.usuario u
          ON u.id_usuario = e.id_usuario
        JOIN convocatoria.convocatoria c
          ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.docente d
          ON d.id_docente = c.id_docente
        JOIN academico.periodo_academico pa
          ON pa.id_periodo_academico = c.id_periodo_academico
        WHERE ra.id_registro_actividad = :idRegistroActividad
          AND d.id_usuario = :idUsuario
          AND pa.activo = true
        LIMIT 1
        """, nativeQuery = true)
    Integer obtenerIdUsuarioAyudantePorActividad(@Param("idUsuario") Integer idUsuario,
                                                @Param("idRegistroActividad") Integer idRegistroActividad);

    /**
     * Devuelve el id_usuario del ayudante (estudiante.usuario) para una evidencia validando ownership.
     */
    @Query(value = """
        SELECT u.id_usuario
        FROM ayudantia.evidencia_registro_actividad ev
        JOIN ayudantia.registro_actividad ra
          ON ra.id_registro_actividad = ev.id_registro_actividad
        JOIN ayudantia.ayudantia a
          ON a.id_ayudantia = ra.id_ayudantia
        JOIN postulacion.postulacion p
          ON p.id_postulacion = a.id_postulacion
        JOIN academico.estudiante e
          ON e.id_estudiante = p.id_estudiante
        JOIN seguridad.usuario u
          ON u.id_usuario = e.id_usuario
        JOIN convocatoria.convocatoria c
          ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.docente d
          ON d.id_docente = c.id_docente
        JOIN academico.periodo_academico pa
          ON pa.id_periodo_academico = c.id_periodo_academico
        WHERE ev.id_evidencia_registro_actividad = :idEvidencia
          AND d.id_usuario = :idUsuario
          AND pa.activo = true
        LIMIT 1
        """, nativeQuery = true)
    Integer obtenerIdUsuarioAyudantePorEvidencia(@Param("idUsuario") Integer idUsuario,
                                                @Param("idEvidencia") Integer idEvidencia);

    /**
     * Obtiene ruta_archivo de evidencia validando ownership.
     */
    @Query(value = """
        SELECT ev.ruta_archivo
        FROM ayudantia.evidencia_registro_actividad ev
        JOIN ayudantia.registro_actividad ra
          ON ra.id_registro_actividad = ev.id_registro_actividad
        JOIN ayudantia.ayudantia a
          ON a.id_ayudantia = ra.id_ayudantia
        JOIN postulacion.postulacion p
          ON p.id_postulacion = a.id_postulacion
        JOIN convocatoria.convocatoria c
          ON c.id_convocatoria = p.id_convocatoria
        JOIN academico.docente d
          ON d.id_docente = c.id_docente
        WHERE ev.id_evidencia_registro_actividad = :idEvidencia
          AND d.id_usuario = :idUsuario
        LIMIT 1
        """, nativeQuery = true)
    String obtenerRutaArchivoEvidencia(@Param("idUsuario") Integer idUsuario,
                                      @Param("idEvidencia") Integer idEvidencia);

    @Query(value = "SELECT id_tipo_estado_registro FROM ayudantia.tipo_estado_registro WHERE UPPER(codigo) = UPPER(:codigo) LIMIT 1", nativeQuery = true)
    Integer getIdEstadoRegistroPorCodigo(@Param("codigo") String codigo);

    @Query(value = "SELECT id_tipo_estado_evidencia FROM ayudantia.tipo_estado_evidencia WHERE UPPER(codigo) = UPPER(:codigo) LIMIT 1", nativeQuery = true)
    Integer getIdEstadoEvidenciaPorCodigo(@Param("codigo") String codigo);
}
