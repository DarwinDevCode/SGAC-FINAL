package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.dto.Request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.service.ILogAuditoriaService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.uteq.sgacfinal.service.IPdfGeneratorService;

import org.uteq.sgacfinal.security.UsuarioPrincipal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/log-auditoria")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LogAuditoriaController {

    private final ILogAuditoriaService logAuditoriaService;
    private final IPdfGeneratorService pdfGeneratorService;

    @GetMapping("/paginado")
    public ResponseEntity<?> obtenerLogsPaginados(
            @RequestParam(required = false) String queryParams,
            @RequestParam(required = false) String tablaAfectada,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaHora") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<LogAuditoriaResponseDTO> logs = logAuditoriaService.obtenerLogsPaginados(
                    queryParams, tablaAfectada, accion, fechaInicio, fechaFin, pageable
            );
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al obtener auditoría: " + e.getMessage()));
        }
    }

    @GetMapping("/reporte-pdf")
    public ResponseEntity<?> descargarReportePdf(
            @RequestParam(required = false) String queryParams,
            @RequestParam(required = false) String tablaAfectada,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        try {
            // Reutilizar el servicio para traer TODOS los resultados filtrados sin paginación (tamaño máximo 10000 para reporte)
            Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "fechaHora"));
            Page<LogAuditoriaResponseDTO> paginaLogs = logAuditoriaService.obtenerLogsPaginados(
                    queryParams, tablaAfectada, accion, fechaInicio, fechaFin, pageable
            );
            List<LogAuditoriaResponseDTO> logs = paginaLogs.getContent();

            // Formar texto de filtros aplicados
            StringBuilder filtros = new StringBuilder();
            if (queryParams != null) filtros.append("Búsqueda='").append(queryParams).append("' ");
            if (tablaAfectada != null) filtros.append("Módulo='").append(tablaAfectada).append("' ");
            if (accion != null) filtros.append("Acción='").append(accion).append("' ");

            byte[] pdfBytes = pdfGeneratorService.generarReporteAuditoria(logs, filtros.toString().trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "reporte_auditoria.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al generar el PDF: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de prueba para generar rápidamente un registro de auditoría con el usuario autenticado.
     * Útil para verificar que el dashboard y el listado muestren datos.
     */
    @PostMapping("/registrar-demo")
    public ResponseEntity<?> registrarDemo(@RequestBody(required = false) Map<String, String> body,
                                           HttpServletRequest request) {
        try {
            Integer idUsuario = getAuthenticatedUserId();
            if (idUsuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay usuario autenticado"));
            }

            String accion = body != null && body.containsKey("accion") ? body.get("accion") : "PRUEBA_DEMO";
            String tabla = body != null && body.containsKey("tabla") ? body.get("tabla") : "demo";
            String detalle = body != null && body.containsKey("detalle") ? body.get("detalle") : "Registro generado para prueba.";

            logAuditoriaService.registrar(LogAuditoriaRequestDTO.builder()
                    .idUsuario(idUsuario)
                    .accion(accion)
                    .tablaAfectada(tabla)
                    .registroAfectado(null)
                    .ipOrigen(request.getRemoteAddr())
                    .valorAnterior(null)
                    .valorNuevo(detalle)
                    .build());

            return ResponseEntity.ok(Map.of("mensaje", "Log demo registrado", "accion", accion, "tabla", tabla));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "No se pudo registrar log demo: " + e.getMessage()));
        }
    }

    private Integer getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up.getIdUsuario();
        }
        return null;
    }
}
