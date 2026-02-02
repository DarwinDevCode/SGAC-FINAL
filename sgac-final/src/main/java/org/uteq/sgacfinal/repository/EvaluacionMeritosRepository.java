package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.EvaluacionMeritos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EvaluacionMeritosRepository extends JpaRepository<EvaluacionMeritos, Integer> {

    @Query(value = "SELECT public.sp_crear_evaluacion_meritos(:idPostulacion, :notaAsig, :notaSem, :notaEventos, :notaExp, :fecha)", nativeQuery = true)
    Integer registrarEvaluacionMeritos(@Param("idPostulacion") Integer idPostulacion,
                                       @Param("notaAsig") BigDecimal notaAsignatura,
                                       @Param("notaSem") BigDecimal notaSemestres,
                                       @Param("notaEventos") BigDecimal notaEventos,
                                       @Param("notaExp") BigDecimal notaExperiencia,
                                       @Param("fecha") LocalDate fechaEvaluacion);

    @Query(value = "SELECT public.sp_actualizar_evaluacion_meritos(:id, :notaAsig, :notaSem, :notaEventos, :notaExp)", nativeQuery = true)
    Integer actualizarEvaluacionMeritos(@Param("id") Integer idEvaluacion,
                                        @Param("notaAsig") BigDecimal notaAsignatura,
                                        @Param("notaSem") BigDecimal notaSemestres,
                                        @Param("notaEventos") BigDecimal notaEventos,
                                        @Param("notaExp") BigDecimal notaExperiencia);

    @Query(value = "SELECT * FROM public.sp_obtener_evaluacion_meritos(:idPostulacion)", nativeQuery = true)
    Optional<EvaluacionMeritos> obtenerPorPostulacionSP(@Param("idPostulacion") Integer idPostulacion);
}