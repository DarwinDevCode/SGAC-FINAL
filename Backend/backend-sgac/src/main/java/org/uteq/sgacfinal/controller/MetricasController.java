package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Response.AdminConsultaDTO;
import org.uteq.sgacfinal.service.impl.NotificacionMasivaService;
import org.uteq.sgacfinal.service.IPdfGeneratorService;
import org.uteq.sgacfinal.service.IExcelGeneratorService;

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
    private final IPdfGeneratorService pdfService;
    private final IExcelGeneratorService excelGeneratorService;

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

    /** GET /api/metricas/reporte-admin */
    @GetMapping("/reporte-admin")
    public ResponseEntity<byte[]> descargarReporteDashboardAdmin() {
        try {
            // Since we don't have an admin dashboard service method yet, we build dummy data for presentation
            String dataJson = "{\"Total Usuarios Registrados\": 150, \"Facultades Activas\": 5, \"Periodos Abiertos\": 2, \"Total Postulaciones Mes\": 45}";
            byte[] pdfBytes = pdfService.generarReporteDashboard(dataJson);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_dashboard.pdf");

            return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reporte-admin/excel")
    public ResponseEntity<byte[]> descargarReporteDashboardAdminExcel() {
        try {
            // Placeholder data for Dashboard as is done in the PDF service
            AdminConsultaDTO mockDto = AdminConsultaDTO.builder()
                .totalUsuarios(150L)
                .totalConvocatorias(2L)
                .totalPostulaciones(45L)
                .periodoActivo("Periodo Prueba")
                .build();
            byte[] excelBytes = excelGeneratorService.generarExcelDashboard(mockDto);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "reporte_dashboard.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(excelBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
