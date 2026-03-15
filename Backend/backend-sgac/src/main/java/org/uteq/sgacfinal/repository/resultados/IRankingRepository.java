package org.uteq.sgacfinal.repository.resultados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.EvaluacionOposicion;

@Repository
public interface IRankingRepository extends JpaRepository<EvaluacionOposicion, Integer> {

    @Query(value = """
        SELECT CAST(
            postulacion.fn_obtener_ranking_resultados(:pIdUsuario, :pRol)
        AS text)
        """, nativeQuery = true)
    String obtenerRankingResultados(
            @Param("pIdUsuario") Integer pIdUsuario,
            @Param("pRol")       String  pRol
    );
}