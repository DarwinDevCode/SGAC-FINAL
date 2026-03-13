package org.uteq.sgacfinal.repository.evaluaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.EvaluacionOposicion;

@Repository
public interface IEvaluacionOposicionRepository
        extends JpaRepository<EvaluacionOposicion, Integer> {

    @Query(value = """
        SELECT CAST(
            postulacion.fn_gestionar_banco_temas(
                :pIdConvocatoria,
                :pAccion,
                CAST(:pTemasJson AS jsonb)
            ) AS text
        )
        """, nativeQuery = true)
    String gestionarBancoTemas(
            @Param("pIdConvocatoria") Integer pIdConvocatoria,
            @Param("pAccion")        String  pAccion,
            @Param("pTemasJson")     String  pTemasJson
    );

    @Query(value = """
        SELECT CAST(
            postulacion.fn_ejecutar_sorteo_oposicion(
                :pIdConvocatoria,
                CAST(:pFecha AS date),
                CAST(:pHoraInicio AS time),
                :pLugar
            ) AS text
        )
        """, nativeQuery = true)
    String ejecutarSorteo(
            @Param("pIdConvocatoria") Integer pIdConvocatoria,
            @Param("pFecha")          String  pFecha,
            @Param("pHoraInicio")     String  pHoraInicio,
            @Param("pLugar")          String  pLugar
    );

    @Query(value = """
        SELECT CAST(
            postulacion.fn_registrar_puntaje_jurado(
                :pIdEval,
                :pIdUsuario,
                CAST(:pMaterial    AS numeric),
                CAST(:pExposicion  AS numeric),
                CAST(:pRespuestas  AS numeric),
                :pFinalizar
            ) AS text
        )
        """, nativeQuery = true)
    String registrarPuntajeJurado(
            @Param("pIdEval")      Integer pIdEval,
            @Param("pIdUsuario")   Integer pIdUsuario,
            @Param("pMaterial")    String  pMaterial,
            @Param("pExposicion")  String  pExposicion,
            @Param("pRespuestas")  String  pRespuestas,
            @Param("pFinalizar")   Boolean pFinalizar
    );

    @Query(value = """
        SELECT CAST(
            postulacion.fn_cambiar_estado_evaluacion(
                :pIdEval,
                :pAccion
            ) AS text
        )
        """, nativeQuery = true)
    String cambiarEstadoEvaluacion(
            @Param("pIdEval")  Integer pIdEval,
            @Param("pAccion")  String  pAccion
    );

    @Query(value = """
        SELECT CAST(
            postulacion.fn_consultar_cronograma_oposicion(:pIdConvocatoria)
        AS text)
        """, nativeQuery = true)
    String consultarCronograma(
            @Param("pIdConvocatoria") Integer pIdConvocatoria
    );
}