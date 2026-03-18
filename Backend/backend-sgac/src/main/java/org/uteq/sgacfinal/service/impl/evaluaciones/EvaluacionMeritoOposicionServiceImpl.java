package org.uteq.sgacfinal.service.impl.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.evaluaciones.BancoTemasRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.CambiarEstadoEvaluacionRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.PuntajeJuradoRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.SorteoOposicionRequest;
import org.uteq.sgacfinal.exception.ComisionException;
import org.uteq.sgacfinal.repository.evaluaciones.IEvaluacionOposicionRepository;
import org.uteq.sgacfinal.service.evaluaciones.IEvaluacionOposicionService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluacionMeritoOposicionServiceImpl implements IEvaluacionOposicionService {

    private final IEvaluacionOposicionRepository repo;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public JsonNode gestionarBancoTemas(BancoTemasRequest request) {
        try {
            String temasJson = "[]";
            if (request.getTemas() != null && !request.getTemas().isEmpty()) {
                List<Map<String, String>> lista = request.getTemas().stream()
                        .map(t -> Map.of("descripcionTema", t.getDescripcionTema()))
                        .collect(Collectors.toList());
                temasJson = objectMapper.writeValueAsString(lista);
            }
            String raw = repo.gestionarBancoTemas(
                    request.getIdConvocatoria(), request.getAccion(), temasJson);

            log.info("\n\n\n\n\n\nRaw JSON : {}", raw);


            return evaluar(raw, "gestionarBancoTemas");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] gestionarBancoTemas", e);
            throw new ComisionException("Error al gestionar banco de temas: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JsonNode ejecutarSorteo(SorteoOposicionRequest request) {
        try {
            String raw = repo.ejecutarSorteo(
                    request.getIdConvocatoria(), request.getFecha(),
                    request.getHoraInicio(), request.getLugar());
            return evaluar(raw, "ejecutarSorteo");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] ejecutarSorteo", e);
            throw new ComisionException("Error al ejecutar sorteo: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JsonNode registrarPuntajeJurado(PuntajeJuradoRequest request) {
        try {
            String raw = repo.registrarPuntajeJurado(
                    request.getIdEvaluacionOposicion(),
                    request.getIdUsuario(),
                    request.getPuntajeMaterial().toPlainString(),
                    request.getPuntajeExposicion().toPlainString(),
                    request.getPuntajeRespuestas().toPlainString(),
                    request.isFinalizar());
            return evaluar(raw, "registrarPuntajeJurado");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] registrarPuntajeJurado", e);
            throw new ComisionException("Error al registrar puntaje: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JsonNode cambiarEstadoEvaluacion(CambiarEstadoEvaluacionRequest request) {
        try {
            String raw = repo.cambiarEstadoEvaluacion(
                    request.getIdEvaluacionOposicion(), request.getAccion());
            return evaluar(raw, "cambiarEstadoEvaluacion");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] cambiarEstadoEvaluacion", e);
            throw new ComisionException("Error al cambiar estado: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JsonNode consultarCronograma(Integer idConvocatoria) {
        try {
            String raw = repo.consultarCronograma(idConvocatoria);
            return evaluar(raw, "consultarCronograma");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] consultarCronograma", e);
            throw new ComisionException("Error al consultar cronograma: " + e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public JsonNode obtenerMiTurno(Integer idConvocatoria, Integer idUsuario) {
        try {
            String raw = repo.obtenerMiTurno(idConvocatoria, idUsuario);
            return evaluar(raw, "obtenerMiTurno");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] obtenerMiTurno", e);
            throw new ComisionException("Error al obtener turno del postulante: " + e.getMessage());
        }
    }


    private JsonNode evaluar(String raw, String contexto) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.isArray() && !node.isEmpty()) {
                JsonNode first = node.get(0);
                if (first.isObject()) {
                    node = first.fields().next().getValue();
                }
            }
            if (!node.path("exito").asBoolean(true)) {
                String msg = node.path("mensaje").asText("Error en " + contexto);
                throw new ComisionException(msg);
            }
            return node;
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] Error parseando respuesta de {}: {}", contexto, raw);
            throw new ComisionException("Respuesta inesperada del servidor.");
        }
    }
}