package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.RegistroActividad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Integer> {

    @Query(value = "SELECT public.sp_crear_registro_actividad(:idAyudantia, :descripcion, :tema, :fecha, :asistentes, :horas, :estado)", nativeQuery = true)
    Integer registrarActividad(@Param("idAyudantia") Integer idAyudantia,
                               @Param("descripcion") String descripcion,
                               @Param("tema") String temaTratado,
                               @Param("fecha") LocalDate fecha,
                               @Param("asistentes") Integer numeroAsistentes,
                               @Param("horas") BigDecimal horasDedicadas,
                               @Param("estado") String estadoRevision);

    @Query(value = "SELECT public.sp_actualizar_registro_actividad(:id, :descripcion, :tema, :horas, :estado)", nativeQuery = true)
    Integer actualizarActividad(@Param("id") Integer idRegistro,
                                @Param("descripcion") String descripcion,
                                @Param("tema") String temaTratado,
                                @Param("horas") BigDecimal horasDedicadas,
                                @Param("estado") String estadoRevision);

    @Query(value = "SELECT * FROM public.sp_listar_actividades_ayudantia(:idAyudantia)", nativeQuery = true)
    List<RegistroActividad> listarActividadesPorAyudantiaSP(@Param("idAyudantia") Integer idAyudantia);




    @Query(value = """
        SELECT * FROM ayudantia.fn_listar_sesiones(
            :idUsuario, :fechaDesde, :fechaHasta, :estado, :idPeriodo
        )
        """, nativeQuery = true)
    List<Object[]> listarSesiones(
            @Param("idUsuario")   Integer idUsuario,
            @Param("fechaDesde")  LocalDate fechaDesde,
            @Param("fechaHasta")  LocalDate fechaHasta,
            @Param("estado")      String estado,
            @Param("idPeriodo")   Integer idPeriodo
    );

    @Query(value = """
        SELECT * FROM ayudantia.fn_detalle_sesion(:idUsuario, :idRegistro)
        """, nativeQuery = true)
    Optional<Object[]> detalleSesion(
            @Param("idUsuario")  Integer idUsuario,
            @Param("idRegistro") Integer idRegistro
    );

    @Query(value = """
        SELECT * FROM ayudantia.fn_registrar_actividad(
            :idUsuario, :idAyudantia, :descripcion, :tema,
            :fecha, :asistentes, :horas, CAST(:evidencias AS jsonb)
        )
        """, nativeQuery = true)
    Object[] registrarActividad(
            @Param("idUsuario")   Integer idUsuario,
            @Param("idAyudantia") Integer idAyudantia,
            @Param("descripcion") String descripcion,
            @Param("tema")        String tema,
            @Param("fecha")       LocalDate fecha,
            @Param("asistentes")  Integer asistentes,
            @Param("horas")       BigDecimal horas,
            @Param("evidencias")  String evidencias
    );

    @Query(value = """
        SELECT COUNT(*) > 0
        FROM ayudantia.registro_actividad ra
        JOIN ayudantia.ayudantia a ON a.id_ayudantia = ra.id_ayudantia
        JOIN postulacion.postulacion pp ON pp.id_postulacion = a.id_postulacion
        JOIN academico.estudiante est ON est.id_estudiante = pp.id_estudiante
        WHERE ra.id_registro_actividad = :idRegistro
          AND est.id_usuario = :idUsuario
        """, nativeQuery = true)
    boolean perteneceAlAyudante(
            @Param("idRegistro") Integer idRegistro,
            @Param("idUsuario")  Integer idUsuario
    );
}