package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.service.IAdminReporteService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AdminReporteController {

    private final IAdminReporteService adminReporteService;

    @GetMapping("/usuarios")
    public ResponseEntity<?> getReporteUsuarios() {
        try {
            return ResponseEntity.ok(adminReporteService.reporteGlobalUsuarios());
        } catch (Exception e) {
            log.error("Error en reporte global usuarios: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/personal")
    public ResponseEntity<?> getReportePersonal() {
        try {
            return ResponseEntity.ok(adminReporteService.reporteGlobalPersonal());
        } catch (Exception e) {
            log.error("Error en reporte global personal: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/postulantes")
    public ResponseEntity<?> getReportePostulantes() {
        try {
            return ResponseEntity.ok(adminReporteService.reporteGlobalPostulantes());
        } catch (Exception e) {
            log.error("Error en reporte global postulantes: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ayudantes")
    public ResponseEntity<?> getReporteAyudantes() {
        try {
            return ResponseEntity.ok(adminReporteService.reporteGlobalAyudantes());
        } catch (Exception e) {
            log.error("Error en reporte global ayudantes: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/auditoria")
    public ResponseEntity<?> getReporteAuditoria() {
        try {
            return ResponseEntity.ok(adminReporteService.reporteAuditoriaGlobal());
        } catch (Exception e) {
            log.error("Error en reporte auditoria global: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
