package org.uteq.sgacfinal.service.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import org.uteq.sgacfinal.dto.Request.evaluaciones.GuardarMeritosRequest;

public interface IEvaluacionMeritosService {
    JsonNode listarPostulacionesParaMeritos(Integer idUsuario);
    JsonNode obtenerEvaluacionMeritos(Integer idPostulacion, Integer idUsuario);
    JsonNode guardarEvaluacionMeritos(GuardarMeritosRequest request, Integer idUsuario);
    JsonNode reabrirEvaluacionMeritos(Integer idPostulacion, Integer idUsuario);
}