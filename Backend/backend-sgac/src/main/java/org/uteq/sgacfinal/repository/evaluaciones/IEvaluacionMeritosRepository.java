package org.uteq.sgacfinal.repository.evaluaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.EvaluacionMeritos;

@Repository
public interface IEvaluacionMeritosRepository
        extends JpaRepository<EvaluacionMeritos, Integer> {

    @Query(value = """
        SELECT CAST(
            postulacion.fn_listar_postulaciones_para_meritos(:pIdUsuario)
        AS text)
        """, nativeQuery = true)
    String listarPostulacionesParaMeritos(
            @Param("pIdUsuario") Integer pIdUsuario
    );

    @Query(value = """
        SELECT CAST(
            postulacion.fn_obtener_evaluacion_meritos(:pIdPostulacion, :pIdUsuario)
        AS text)
        """, nativeQuery = true)
    String obtenerEvaluacionMeritos(
            @Param("pIdPostulacion") Integer pIdPostulacion,
            @Param("pIdUsuario")     Integer pIdUsuario
    );

    @Query(value = """
        SELECT CAST(
            postulacion.fn_guardar_evaluacion_meritos(
                :pIdPostulacion,
                :pIdUsuario,
                CAST(:pNotaAsignaturaRaw AS numeric),
                CAST(:pSemestresJson     AS jsonb),
                CAST(:pNotaExperiencia   AS numeric),
                CAST(:pNotaEventos       AS numeric),
                :pFinalizar
            ) AS text
        )
        """, nativeQuery = true)
    String guardarEvaluacionMeritos(
            @Param("pIdPostulacion")      Integer pIdPostulacion,
            @Param("pIdUsuario")          Integer pIdUsuario,
            @Param("pNotaAsignaturaRaw")  String  pNotaAsignaturaRaw,
            @Param("pSemestresJson")       String  pSemestresJson,
            @Param("pNotaExperiencia")    String  pNotaExperiencia,
            @Param("pNotaEventos")        String  pNotaEventos,
            @Param("pFinalizar")          Boolean pFinalizar
    );

    @Query(value = """
        SELECT CAST(
            postulacion.fn_reabrir_evaluacion_meritos(:pIdPostulacion, :pIdUsuario)
        AS text)
        """, nativeQuery = true)
    String reabrirEvaluacionMeritos(
            @Param("pIdPostulacion") Integer pIdPostulacion,
            @Param("pIdUsuario")     Integer pIdUsuario
    );
}