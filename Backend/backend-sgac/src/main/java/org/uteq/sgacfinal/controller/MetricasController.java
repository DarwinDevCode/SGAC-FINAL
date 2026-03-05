package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.service.impl.NotificacionMasivaService;

import java.util.Map;

/**
 * P10 (Ítems 11/12/13): Endpoints para notificaciones masivas.
 * P12 (Ítem 3): Endpoints para KPIs de métricas de convocatoria.
 */
@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MetricasController {

    private final NotificacionMasivaService masivaService;


    /** GET /api/metricas/coordinador?idCarrera=X */
    @GetMapping("/coordinador")
    public ResponseEntity<?> metricasCoordinador(@RequestParam Integer idCarrera) {
        try {
            return ResponseEntity.ok(masivaService.dashboardCoordinador(idCarrera));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al cargar métricas: " + e.getMessage()));
        }
    }

    /** GET /api/metricas/postulante?idUsuario=X */
    @GetMapping("/postulante")
    public ResponseEntity<?> metricasPostulante(@RequestParam Integer idUsuario) {
        try {
            return ResponseEntity.ok(masivaService.dashboardPostulante(idUsuario));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al cargar métricas: " + e.getMessage()));
        }
    }

    // ---- P10: Notificaciones Masivas ----

    /**
     * POST /api/metricas/notificaciones/masiva
     * Body: { "mensaje": "...", "tipo": "CONVOCATORIA", "tipoNotificacion": "MASIVA_ROL",
     *         "idRol": 2, "idConvocatoria": null }
     */
    @PostMapping("/notificaciones/masiva")
    public ResponseEntity<?> enviarMasiva(@RequestBody Map<String, Object> body) {
        try {
            String mensaje          = (String)  body.get("mensaje");
            String tipo             = (String)  body.getOrDefault("tipo", "GENERAL");
            String tipoNotificacion = (String)  body.getOrDefault("tipoNotificacion", "MASIVA_TODOS");
            Integer idRol           = body.get("idRol") != null ? (Integer) body.get("idRol") : null;
            Integer idConvocatoria  = body.get("idConvocatoria") != null ? (Integer) body.get("idConvocatoria") : null;

            int count = masivaService.enviarMasiva(mensaje, tipo, tipoNotificacion, idRol, idConvocatoria);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Notificación enviada a " + count + " usuario(s).",
                    "cantidadEnviada", count
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al enviar notificación masiva: " + e.getMessage()));
        }
    }
}
