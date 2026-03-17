package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Ayudantia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AyudantiaRepository extends JpaRepository<Ayudantia, Integer> {

    @Query(value = "SELECT public.sp_crear_ayudantia(:idEstado, :idPostulacion, :inicio, :fin, :horas)", nativeQuery = true)
    Integer registrarAyudantia(@Param("idEstado") Integer idTipoEstado,
                               @Param("idPostulacion") Integer idPostulacion,
                               @Param("inicio") LocalDate fechaInicio,
                               @Param("fin") LocalDate fechaFin,
                               @Param("horas") Integer horasCumplidas);

    @Query(value = "SELECT public.sp_actualizar_ayudantia(:id, :idEstado, :inicio, :fin, :horas)", nativeQuery = true)
    Integer actualizarAyudantia(@Param("id") Integer idAyudantia,
                                @Param("idEstado") Integer idTipoEstado,
                                @Param("inicio") LocalDate fechaInicio,
                                @Param("fin") LocalDate fechaFin,
                                @Param("horas") Integer horasCumplidas);

    @Query(value = "SELECT * FROM public.sp_obtener_ayudantia_por_id(:id)", nativeQuery = true)
    Optional<Ayudantia> buscarPorIdSP(@Param("id") Integer id);


    @Query("SELECT a FROM Ayudantia a " +
            "LEFT JOIN FETCH a.registrosActividad ra " +
            "LEFT JOIN FETCH ra.evidencias " +
            "WHERE a.idAyudantia = :idAyudantia")
    Optional<Ayudantia> findAyudantiaConDetalles(@Param("idAyudantia") Integer idAyudantia);

    @Query(value = """
    SELECT ra.* FROM ayudantia.registro_actividad ra
    WHERE ra.id_ayudantia = (
        SELECT a.id_ayudantia 
        FROM ayudantia.ayudantia a
        JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
        JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
        WHERE e.id_usuario = :idUsuario AND a.id_tipo_estado_ayudantia = 1
        LIMIT 1
    )
    ORDER BY ra.fecha DESC
    """, nativeQuery = true)
    List<Object[]> findActividadesRawByUsuario(@Param("idUsuario") Integer idUsuario);

    /**
     * Lista todas las sesiones/actividades (RegistroActividad) asociadas al ayudante.
     *
     * Ahora se usa nativeQuery para traer el nombre del estado (tipo_estado_registro)
     * y las observaciones/fecha_observacion del registro.
     */
    @Query(value = """
            SELECT
                ra.id_registro_actividad,
                ra.descripcion_actividad,
                ra.tema_tratado,
                ra.fecha,
                ra.numero_asistentes,
                ra.horas_dedicadas,
                ra.id_tipo_estado_registro,
                ter.nombre_estado AS nombre_estado,
                ra.observaciones,
                ra.fecha_observacion
            FROM ayudantia.registro_actividad ra
            JOIN ayudantia.tipo_estado_registro ter
                ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
            JOIN ayudantia.ayudantia a
                ON a.id_ayudantia = ra.id_ayudantia
            JOIN postulacion.postulacion p
                ON p.id_postulacion = a.id_postulacion
            JOIN academico.estudiante e
                ON e.id_estudiante = p.id_estudiante
            JOIN seguridad.usuario u
                ON u.id_usuario = e.id_usuario
            WHERE u.id_usuario = :idAyudante
            ORDER BY ra.fecha DESC, ra.id_registro_actividad DESC
            """, nativeQuery = true)
    List<Object[]> findAllByAyudanteId(@Param("idAyudante") Integer idAyudante);

    /**
     * Recupera SOLO las evidencias de una sesión/actividad.
     *
     * Se filtra por idRegistroActividad y por idAyudante para asegurar que
     * el ayudante solo acceda a evidencias de sus propias actividades.
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
                ev.observaciones AS observacion_evidencia,
                ev.fecha_observacion AS fecha_observacion_evidencia
            FROM ayudantia.registro_actividad ra
            JOIN ayudantia.ayudantia a
                ON a.id_ayudantia = ra.id_ayudantia
            JOIN postulacion.postulacion p
                ON p.id_postulacion = a.id_postulacion
            JOIN academico.estudiante e
                ON e.id_estudiante = p.id_estudiante
            JOIN seguridad.usuario u
                ON u.id_usuario = e.id_usuario
            LEFT JOIN ayudantia.evidencia_registro_actividad ev
                ON ev.id_registro_actividad = ra.id_registro_actividad
            LEFT JOIN ayudantia.tipo_estado_evidencia tee
                ON tee.id_tipo_estado_evidencia = ev.id_tipo_estado_evidencia
            WHERE ra.id_registro_actividad = :idRegistroActividad
              AND u.id_usuario = :idAyudante
            ORDER BY ev.id_evidencia_registro_actividad
            """, nativeQuery = true)
    List<Object[]> findDetalleConEvidenciasById(
            @Param("idAyudante") Integer idAyudante,
            @Param("idRegistroActividad") Integer idRegistroActividad
    );

    @Query(value = "SELECT ayudantia.fn_obtener_id_ayudantia_por_usuario(:idUsuario)", nativeQuery = true)
    Optional<Integer> ayudantiaPorUsuario(@Param("idUsuario") Integer idUsuario);
}