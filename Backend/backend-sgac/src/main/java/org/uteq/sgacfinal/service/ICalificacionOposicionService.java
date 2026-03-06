package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.CalificacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Response.CalificacionOposicionResponseDTO;
import org.uteq.sgacfinal.dto.Response.OposicionEstadoResponseDTO;
import org.uteq.sgacfinal.dto.Response.RankingEvaluacionDTO;

import java.util.List;

public interface ICalificacionOposicionService {

    /**
     * Guarda o actualiza la nota individual de un miembro del tribunal.
     * Cuando los 3 miembros han calificado, calcula y persiste el promedio en resumen_evaluacion.
     */
    CalificacionOposicionResponseDTO guardarNota(CalificacionOposicionRequestDTO request);

    /**
     * Retorna el estado actual de la oposición para una postulación:
     * qué miembros han calificado, y el promedio (si los 3 ya calificaron).
     */
    OposicionEstadoResponseDTO obtenerEstado(Integer idPostulacion);

    /**
     * Obtiene la calificación de un evaluador específico para una postulación.
     */
    CalificacionOposicionResponseDTO obtenerCalificacion(Integer idPostulacion, Integer idEvaluador);

    /**
     * Retorna el ranking completo de una convocatoria con estados y datos de desempate.
     */
    List<RankingEvaluacionDTO> obtenerRankingConvocatoria(Integer idConvocatoria);
}
