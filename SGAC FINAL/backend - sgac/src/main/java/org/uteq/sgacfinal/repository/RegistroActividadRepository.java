package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.RegistroActividad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
}