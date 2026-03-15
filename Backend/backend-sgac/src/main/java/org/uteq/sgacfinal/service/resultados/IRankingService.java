package org.uteq.sgacfinal.service.resultados;

import com.fasterxml.jackson.databind.JsonNode;

public interface IRankingService {
    JsonNode obtenerRankingResultados(Integer idUsuario, String rol);
}