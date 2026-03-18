package org.uteq.sgacfinal.repository.ayudantia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.uteq.sgacfinal.entity.RegistroActividad;

@Repository
public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Integer> {
    @Query(value = "SELECT ayudantia.fn_planificar_actividad(" +
            ":idAyudantia, :fecha, CAST(:horaInicio AS TIME), CAST(:horaFin AS TIME), :lugar, :tema)",
            nativeQuery = true)
    Integer llamarFuncionPlanificar(
            @Param("idAyudantia") Integer idAyudantia,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("lugar") String lugar,
            @Param("tema") String tema
    );

    @Query(value = "SELECT ayudantia.fn_completar_actividad(" +
            ":idRegistroActividad, :descripcionActividad)",
            nativeQuery = true)
    Boolean llamarFuncionCompletar(
            @Param("idRegistroActividad") Integer idRegistroActividad,
            @Param("descripcionActividad") String descripcionActividad
    );

    @Query(value = "SELECT ayudantia.fn_evaluar_actividad(" +
            ":idRegistroActividad, :codigoNuevoEstado, :observaciones)",
            nativeQuery = true)
    Boolean llamarFuncionEvaluar(
            @Param("idRegistroActividad") Integer idRegistroActividad,
            @Param("codigoNuevoEstado") String codigoNuevoEstado,
            @Param("observaciones") String observaciones
    );

}