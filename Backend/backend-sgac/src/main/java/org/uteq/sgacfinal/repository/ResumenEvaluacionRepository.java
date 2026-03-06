package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.ResumenEvaluacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumenEvaluacionRepository extends JpaRepository<ResumenEvaluacion, Integer> {

    @Query("SELECT r FROM ResumenEvaluacion r WHERE r.postulacion.idPostulacion = :idPostulacion")
    Optional<ResumenEvaluacion> findByIdPostulacion(@Param("idPostulacion") Integer idPostulacion);

    /** Todos los resúmenes de una convocatoria (para publicar resultados y notificar) */
    @Query("SELECT r FROM ResumenEvaluacion r WHERE r.postulacion.convocatoria.idConvocatoria = :idConvocatoria")
    List<ResumenEvaluacion> findAllByConvocatoria(@Param("idConvocatoria") Integer idConvocatoria);

    @Query(value = "SELECT * FROM public.sp_calcular_ranking_evaluacion(:idConvocatoria)", nativeQuery = true)
    List<Object[]> calcularRankingConvocatoria(@Param("idConvocatoria") Integer idConvocatoria);
}
