package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.DictaminarPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarDocumentoRequestDTO;
import org.uteq.sgacfinal.dto.response.*;

import java.util.List;

/**
 * Service interface para la evaluación de postulaciones por parte del coordinador
 */
public interface IEvaluacionPostulacionService {

    /**
     * Lista todas las postulaciones de la carrera del coordinador
     * @param idUsuario ID del usuario coordinador
     * @return Lista de postulaciones
     */
    List<PostulacionListadoCoordinadorDTO> listarPostulacionesCoordinador(Integer idUsuario);

    /**
     * Obtiene el detalle completo de una postulación
     * @param idUsuario ID del usuario coordinador
     * @param idPostulacion ID de la postulación
     * @return Detalle completo de la postulación
     */
    DetallePostulacionCoordinadorDTO obtenerDetallePostulacion(Integer idUsuario, Integer idPostulacion);

    /**
     * Evalúa un documento individual (VALIDAR, OBSERVAR, RECHAZAR)
     * @param idUsuario ID del usuario coordinador
     * @param request DTO con los datos de evaluación
     * @return Resultado de la evaluación
     */
    EvaluacionDocumentoResponseDTO evaluarDocumento(Integer idUsuario, EvaluarDocumentoRequestDTO request);

    /**
     * Dictamina (aprueba o rechaza) una postulación completa
     * @param idUsuario ID del usuario coordinador
     * @param request DTO con los datos del dictamen
     * @return Resultado del dictamen
     */
    DictamenPostulacionResponseDTO dictaminarPostulacion(Integer idUsuario, DictaminarPostulacionRequestDTO request);

    /**
     * Cambia el estado de una postulación a EN_REVISION
     * @param idUsuario ID del usuario coordinador
     * @param idPostulacion ID de la postulación
     * @return Resultado del cambio de estado
     */
    CambioEstadoRevisionResponseDTO cambiarEstadoARevision(Integer idUsuario, Integer idPostulacion);

    /**
     * Obtiene el archivo de un documento adjunto
     * @param idUsuario ID del usuario coordinador
     * @param idRequisitoAdjunto ID del requisito adjunto
     * @return Bytes del archivo
     */
    byte[] obtenerArchivoDocumento(Integer idUsuario, Integer idRequisitoAdjunto);
}

