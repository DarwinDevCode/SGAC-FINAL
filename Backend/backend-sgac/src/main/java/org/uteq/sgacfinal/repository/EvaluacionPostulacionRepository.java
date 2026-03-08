package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Postulacion;

import java.util.List;

/**
 * Repository para las funciones de evaluación de postulaciones por parte del coordinador
 */
@Repository
public interface EvaluacionPostulacionRepository extends JpaRepository<Postulacion, Integer> {

    /**
     * Lista todas las postulaciones de la carrera del coordinador
     */
    @Query(nativeQuery = true, value = """
        SELECT * FROM postulacion.fn_listar_postulaciones_coordinador(:idUsuario)
    """)
    List<Object[]> listarPostulacionesCoordinador(@Param("idUsuario") Integer idUsuario);

    /**
     * Obtiene el detalle completo de una postulación en formato JSON
     */
    @Query(nativeQuery = true, value = """
        SELECT postulacion.fn_obtener_detalle_postulacion_coordinador(:idUsuario, :idPostulacion)::TEXT
    """)
    String obtenerDetallePostulacion(
            @Param("idUsuario") Integer idUsuario,
            @Param("idPostulacion") Integer idPostulacion
    );

    /**
     * Evalúa un documento individual (VALIDAR, OBSERVAR, RECHAZAR)
     */
    @Query(nativeQuery = true, value = """
        SELECT postulacion.fn_evaluar_documento_individual(:idUsuario, :idRequisitoAdjunto, :accion, :observacion)::TEXT
    """)
    String evaluarDocumentoIndividual(
            @Param("idUsuario") Integer idUsuario,
            @Param("idRequisitoAdjunto") Integer idRequisitoAdjunto,
            @Param("accion") String accion,
            @Param("observacion") String observacion
    );

    /**
     * Dictamina (aprueba o rechaza) una postulación completa
     */
    @Query(nativeQuery = true, value = """
        SELECT postulacion.fn_dictaminar_postulacion(:idUsuario, :idPostulacion, :accion, :observacion)::TEXT
    """)
    String dictaminarPostulacion(
            @Param("idUsuario") Integer idUsuario,
            @Param("idPostulacion") Integer idPostulacion,
            @Param("accion") String accion,
            @Param("observacion") String observacion
    );

    /**
     * Cambia el estado de una postulación a EN_REVISION cuando el coordinador la abre
     */
    @Query(nativeQuery = true, value = """
        SELECT postulacion.fn_cambiar_estado_postulacion_revision(:idUsuario, :idPostulacion)::TEXT
    """)
    String cambiarEstadoARevision(
            @Param("idUsuario") Integer idUsuario,
            @Param("idPostulacion") Integer idPostulacion
    );
}

