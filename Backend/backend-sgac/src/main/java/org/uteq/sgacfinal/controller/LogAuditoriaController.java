package org.uteq.sgacfinal.controller;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/log-auditoria")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LogAuditoriaController {

    private final ILogAuditoriaService logAuditoriaService;
    private final IPdfGeneratorService pdfGeneratorService;
    private final org.uteq.sgacfinal.service.IExcelGeneratorService excelGeneratorService;

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
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_auditoria.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Error al generar PDF de auditoría: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al generar el PDF: " + e.getMessage()));
        }
    }

    @GetMapping("/reporte-excel")
    public ResponseEntity<?> descargarReporteExcel(
            @RequestParam(required = false) String queryParams,
            @RequestParam(required = false) String tablaAfectada,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        try {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "fechaHora"));
            Page<LogAuditoriaResponseDTO> paginaLogs = logAuditoriaService.obtenerLogsPaginados(
                    queryParams, tablaAfectada, accion, fechaInicio, fechaFin, pageable
            );
            List<LogAuditoriaResponseDTO> logs = paginaLogs.getContent();

            byte[] excelBytes = excelGeneratorService.generarExcelAuditoria(logs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_auditoria.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            log.error("Error al generar Excel de auditoría: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al generar el Excel: " + e.getMessage()));
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
