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
            postulacion.fn_consultar_mi_turno(:pIdUsuario, :pIdConvocatoria)
        AS text)
        """, nativeQuery = true)
    String consultarMiTurno(
            @Param("pIdUsuario")      Integer pIdUsuario,
            @Param("pIdConvocatoria") Integer pIdConvocatoria
    );

    @Query(value = """
        SELECT row_to_json(t) FROM (
            SELECT
                COALESCE(co.max_puntaje_material,   10.0) AS "maxPuntajeMaterial",
                COALESCE(co.max_puntaje_exposicion,  4.0) AS "maxPuntajeExposicion",
                COALESCE(co.max_puntaje_respuestas,  6.0) AS "maxPuntajeRespuestas",
                COALESCE(co.minutos_exposicion,       20)  AS "minutosExposicion",
                COALESCE(co.minutos_preguntas,        10)  AS "minutosPreguntas",
                COALESCE(co.minutos_transicion,        5)  AS "minutosTransicion"
            FROM convocatoria.convocatoria c
            LEFT JOIN postulacion.configuracion_oposicion co
                   ON co.id_convocatoria = c.id_convocatoria
            WHERE c.id_convocatoria = :pIdConvocatoria
        ) t
        """, nativeQuery = true)
    String obtenerConfiguracion(@Param("pIdConvocatoria") Integer pIdConvocatoria);

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

    @Query(value = """
        SELECT CAST(
            postulacion.fn_obtener_mi_turno(
                :pIdConvocatoria,
                :pIdUsuario
            ) AS text
        )
        """, nativeQuery = true)
    String obtenerMiTurno(
            @Param("pIdConvocatoria") Integer pIdConvocatoria,
            @Param("pIdUsuario")      Integer pIdUsuario
    );

}