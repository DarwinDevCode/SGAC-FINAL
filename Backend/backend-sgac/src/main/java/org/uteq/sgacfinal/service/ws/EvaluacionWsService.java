package org.uteq.sgacfinal.service.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.ws.EvaluacionWsMessage;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluacionWsService {
    private static final String TOPIC_BASE = "/topic/evaluacion/";
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCambioEstado(Integer idConvocatoria,
                                      Integer idEvaluacion,
                                      String  nuevoEstado,
                                      String  nombreEstado,
                                      String  horaInicioReal,
                                      String  serverTimestamp,   // ← nuevo
                                      String  horaFinReal,
                                      Double  puntajeFinal,
                                      String  mensaje) {
        EvaluacionWsMessage msg = EvaluacionWsMessage.builder()
                .tipo("CAMBIO_ESTADO")
                .idConvocatoria(idConvocatoria)
                .idEvaluacionOposicion(idEvaluacion)
                .nuevoEstado(nuevoEstado)
                .nombreEstado(nombreEstado)
                .horaInicioReal(horaInicioReal)
                .serverTimestamp(serverTimestamp)           // ← nuevo
                .horaFinReal(horaFinReal)
                .puntajeFinal(puntajeFinal)
                .mensaje(mensaje)
                .timestamp(Instant.now().toString())
                .build();

        broadcast(idConvocatoria, msg);
    }

    public void broadcastPuntajeActualizado(Integer idConvocatoria,
                                            Integer idEvaluacion,
                                            Integer idUsuario,
                                            Boolean todosFinalizaron,
                                            Double  puntajeFinal,
                                            String  mensaje) {
        EvaluacionWsMessage msg = EvaluacionWsMessage.builder()
                .tipo("PUNTAJE_ACTUALIZADO")
                .idConvocatoria(idConvocatoria)
                .idEvaluacionOposicion(idEvaluacion)
                .idUsuario(idUsuario)
                .todosFinalizaron(todosFinalizaron)
                .puntajeFinal(puntajeFinal)
                .mensaje(mensaje)
                .timestamp(Instant.now().toString())
                .build();

        broadcast(idConvocatoria, msg);
    }

    private void broadcast(Integer idConvocatoria, EvaluacionWsMessage msg) {
        String destino = TOPIC_BASE + idConvocatoria;
        try {
            messagingTemplate.convertAndSend(destino, msg);
            log.debug("[WS-EVAL] → {} | tipo={} estado={}", destino, msg.getTipo(), msg.getNuevoEstado());
        } catch (Exception e) {
            log.warn("[WS-EVAL] Fallo al broadcast en {}: {}", destino, e.getMessage());
        }
    }
}
// Forzado de compilacion