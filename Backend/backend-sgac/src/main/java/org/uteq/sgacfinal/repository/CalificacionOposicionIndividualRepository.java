package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.CalificacionOposicionIndividual;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionOposicionIndividualRepository
        extends JpaRepository<CalificacionOposicionIndividual, Integer> {

    @Query("SELECT c FROM CalificacionOposicionIndividual c WHERE c.postulacion.idPostulacion = :idPostulacion")
    List<CalificacionOposicionIndividual> findByIdPostulacion(@Param("idPostulacion") Integer idPostulacion);

    @Query("SELECT c FROM CalificacionOposicionIndividual c WHERE c.postulacion.idPostulacion = :idPostulacion AND c.idEvaluador = :idEvaluador")
    Optional<CalificacionOposicionIndividual> findByIdPostulacionAndIdEvaluador(
            @Param("idPostulacion") Integer idPostulacion,
            @Param("idEvaluador") Integer idEvaluador);

    @Query(value = "SELECT public.sp_guardar_oposicion_individual(:idPostulacion, :idEvaluador, :rol, :material, :calidad, :pertinencia)",
           nativeQuery = true)
    Integer guardarOposicionIndividual(
            @Param("idPostulacion") Integer idPostulacion,
            @Param("idEvaluador") Integer idEvaluador,
            @Param("rol") String rolEvaluador,
            @Param("material") BigDecimal material,
            @Param("calidad") BigDecimal calidad,
            @Param("pertinencia") BigDecimal pertinencia);

    @Query("SELECT COUNT(c) FROM CalificacionOposicionIndividual c WHERE c.postulacion.idPostulacion = :idPostulacion")
    Long countByIdPostulacion(@Param("idPostulacion") Integer idPostulacion);
}
