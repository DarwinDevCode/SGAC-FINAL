package org.uteq.sgacfinal.service.impl.evaluaciones;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.evaluaciones.ConvocatoriaOposicionDTO;
import org.uteq.sgacfinal.exception.ComisionException;
import org.uteq.sgacfinal.repository.evaluaciones.IEvaluacionOposicionRepository;
import org.uteq.sgacfinal.service.evaluaciones.IEvaluacionOposicionService;
import org.uteq.sgacfinal.service.ws.EvaluacionWsService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluacionMeritoOposicionServiceImpl implements IEvaluacionOposicionService {

    private final IEvaluacionOposicionRepository repo;
    private final ObjectMapper objectMapper;
    private final EvaluacionWsService wsService;

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
    public JsonNode cambiarEstadoEvaluacion(CambiarEstadoEvaluacionRequest request) {
        try {
            String raw = repo.cambiarEstadoEvaluacion(
                    request.getIdEvaluacionOposicion(), request.getAccion());
            JsonNode node = evaluar(raw, "cambiarEstadoEvaluacion");

            // ── Broadcast WebSocket ───────────────────────────────────
            if (request.getIdConvocatoria() != null) {
                emitirCambioEstado(request, node);
            }

            return node;
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] cambiarEstadoEvaluacion", e);
            throw new ComisionException("Error al cambiar estado: " + e.getMessage());
        }
    }

    private void emitirCambioEstado(CambiarEstadoEvaluacionRequest request, JsonNode result) {
        String accion = request.getAccion().toUpperCase();

        String nuevoEstado;
        String nombreEstado;
        switch (accion) {
            case "INICIAR"      -> { nuevoEstado = "EN_CURSO";     nombreEstado = "En Curso";       }
            case "FINALIZAR"    -> { nuevoEstado = "FINALIZADA";   nombreEstado = "Finalizada";     }
            case "NO_PRESENTO"  -> { nuevoEstado = "NO_PRESENTO";  nombreEstado = "No Se Presentó"; }
            default             -> { nuevoEstado = accion;         nombreEstado = accion;            }
        }

        String horaInicioReal  = result.path("horaReal").asText(null);
        String serverTimestamp = result.path("serverTimestamp").asText(null);
        String horaFinReal     = result.path("horaFin").asText(null);
        Double puntajeFinal    = result.path("puntajeFinal").isNull()
                ? null : result.path("puntajeFinal").asDouble();
        String mensaje         = result.path("mensaje").asText(null);

        wsService.broadcastCambioEstado(
                request.getIdConvocatoria(),
                request.getIdEvaluacionOposicion(),
                nuevoEstado,
                nombreEstado,
                horaInicioReal,
                serverTimestamp,        // ← nuevo
                horaFinReal,
                puntajeFinal,
                mensaje
        );
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
            JsonNode node = evaluar(raw, "registrarPuntajeJurado");

            if (request.getIdConvocatoria() != null) {
                boolean todosFinalizaron = node.path("todosFinalizaron").asBoolean(false);
                Double  puntajeFinal     = todosFinalizaron
                        ? node.path("puntajeFinal").asDouble() : null;
                wsService.broadcastPuntajeActualizado(
                        request.getIdConvocatoria(),
                        request.getIdEvaluacionOposicion(),
                        request.getIdUsuario(),
                        todosFinalizaron,
                        puntajeFinal,
                        node.path("mensaje").asText(null)
                );
            }

            return node;
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] registrarPuntajeJurado", e);
            throw new ComisionException("Error al registrar puntaje: " + e.getMessage());
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

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<ConvocatoriaOposicionDTO>> listarConvocatoriasParaOposicion() {
        try {
            String json = repo.listarConvocatoriasParaOposicion();
            return objectMapper.readValue(
                    json,
                    new TypeReference<StandardResponseDTO<List<ConvocatoriaOposicionDTO>>>() {});
        } catch (Exception e) {
            log.error("[ConvocatoriaOposicion] Error al listar convocatorias aptas: {}", e.getMessage());
            return StandardResponseDTO.<List<ConvocatoriaOposicionDTO>>builder()
                    .exito(false)
                    .mensaje("Error al obtener las convocatorias para oposición: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JsonNode resolverSalaUsuario(Integer idUsuario) {
        try {
            String raw = repo.resolverSalaUsuario(idUsuario);
            return evaluar(raw, "resolverSalaUsuario");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] resolverSalaUsuario", e);
            throw new ComisionException("Error al resolver la sala: " + e.getMessage());
        }
    }

    private JsonNode evaluar(String raw, String contexto) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.isArray() && !node.isEmpty()) {
                JsonNode first = node.get(0);
                if (first.isObject()) node = first.fields().next().getValue();
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

    @Override
    @Transactional(readOnly = true)
    public JsonNode resolverMiSala(Integer idUsuario) {
        try {
            String raw = repo.resolverSalaUsuario(idUsuario);
            return evaluar(raw, "resolverMiSala");
        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EvaluacionOposicion] resolverMiSala idUsuario={}", idUsuario, e);
            throw new ComisionException("Error al resolver la sala del usuario: " + e.getMessage());
        }
    }
}