package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.EvaluacionOposicion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface EvaluacionOposicionRepository extends JpaRepository<EvaluacionOposicion, Integer> {

    @Query(value = "SELECT public.sp_crear_evaluacion_oposicion(:idPostulacion, :tema, :fecha, :inicio, :fin, :lugar, :estado)", nativeQuery = true)
    Integer registrarEvaluacionOposicion(@Param("idPostulacion") Integer idPostulacion,
                                         @Param("tema") String temaExposicion,
                                         @Param("fecha") LocalDate fechaEvaluacion,
                                         @Param("inicio") LocalTime horaInicio,
                                         @Param("fin") LocalTime horaFin,
                                         @Param("lugar") String lugar,
                                         @Param("estado") String estado);

    @Query(value = "SELECT public.sp_actualizar_evaluacion_oposicion(:id, :tema, :fecha, :inicio, :fin, :lugar, :estado)", nativeQuery = true)
    Integer actualizarEvaluacionOposicion(@Param("id") Integer idEvaluacion,
                                          @Param("tema") String temaExposicion,
                                          @Param("fecha") LocalDate fechaEvaluacion,
                                          @Param("inicio") LocalTime horaInicio,
                                          @Param("fin") LocalTime horaFin,
                                          @Param("lugar") String lugar,
                                          @Param("estado") String estado);

    @Query(value = "SELECT * FROM public.sp_listar_evaluaciones_oposicion()", nativeQuery = true)
    List<Object[]> listarEvaluacionesOposicionSP();
}