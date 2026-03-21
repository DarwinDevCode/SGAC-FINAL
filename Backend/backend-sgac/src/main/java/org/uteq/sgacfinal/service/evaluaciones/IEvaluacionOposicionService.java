package org.uteq.sgacfinal.service.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import org.uteq.sgacfinal.dto.request.evaluaciones.BancoTemasRequest;
import org.uteq.sgacfinal.dto.request.evaluaciones.CambiarEstadoEvaluacionRequest;
import org.uteq.sgacfinal.dto.request.evaluaciones.PuntajeJuradoRequest;
import org.uteq.sgacfinal.dto.request.evaluaciones.SorteoOposicionRequest;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.evaluaciones.ConvocatoriaOposicionDTO;

import java.util.List;

public interface IEvaluacionOposicionService {
    JsonNode gestionarBancoTemas(BancoTemasRequest request);
    JsonNode ejecutarSorteo(SorteoOposicionRequest request);
    JsonNode registrarPuntajeJurado(PuntajeJuradoRequest request);
    JsonNode cambiarEstadoEvaluacion(CambiarEstadoEvaluacionRequest request);
    JsonNode consultarCronograma(Integer idConvocatoria);
    JsonNode obtenerMiTurno(Integer idConvocatoria, Integer idUsuario);
    StandardResponseDTO<List<ConvocatoriaOposicionDTO>> listarConvocatoriasParaOposicion();
    JsonNode resolverSalaUsuario(Integer idUsuario);
    JsonNode resolverMiSala(Integer idUsuario);

}
