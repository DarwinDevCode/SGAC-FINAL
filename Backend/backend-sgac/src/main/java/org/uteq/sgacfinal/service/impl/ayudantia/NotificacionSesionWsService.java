package org.uteq.sgacfinal.service.impl.ayudantia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionSesionWsService {
    private static final String TOPIC_BASE = "/topic/notificaciones/usuario/";
    private final SimpMessagingTemplate messagingTemplate;

    public void enviarNotificacion(Integer idUsuarioDestino, String titulo, String mensaje, Integer idReferencia) {
        String destino = TOPIC_BASE + idUsuarioDestino;

        Map<String, Object> payload = Map.of(
                "tipo", "NUEVA_SESION_REVISION",
                "titulo", titulo,
                "mensaje", mensaje,
                "idReferencia", idReferencia,
                "timestamp", Instant.now().toString()
        );

        try {
            messagingTemplate.convertAndSend(destino, payload);
            log.debug("[WS-SESION] Notificación enviada a {}: {}", destino, titulo);
        } catch (Exception e) {
            log.warn("[WS-SESION] Fallo al enviar WebSocket a {}: {}", destino, e.getMessage());
        }
    }
}