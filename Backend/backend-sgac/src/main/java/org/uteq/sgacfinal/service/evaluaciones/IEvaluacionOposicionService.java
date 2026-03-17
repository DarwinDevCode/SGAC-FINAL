package org.uteq.sgacfinal.service.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import org.uteq.sgacfinal.dto.Request.evaluaciones.BancoTemasRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.CambiarEstadoEvaluacionRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.PuntajeJuradoRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.SorteoOposicionRequest;

public interface IEvaluacionOposicionService {
    JsonNode gestionarBancoTemas(BancoTemasRequest request);
    JsonNode ejecutarSorteo(SorteoOposicionRequest request);
    JsonNode registrarPuntajeJurado(PuntajeJuradoRequest request);
    JsonNode cambiarEstadoEvaluacion(CambiarEstadoEvaluacionRequest request);
    JsonNode consultarCronograma(Integer idConvocatoria);
    JsonNode obtenerMiTurno(Integer idConvocatoria, Integer idUsuario);
}
