package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.service.IAdminReporteService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AdminReporteController {

    private final IAdminReporteService adminReporteService;

    @GetMapping("/auditoria")
    public ResponseEntity<List<LogAuditoriaResponseDTO>> getAuditoria(
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta) {
        return ResponseEntity.ok(adminReporteService.reporteAuditoriaCompleto(idUsuario, modulo, fechaDesde, fechaHasta));
    }

    @GetMapping("/facultades")
    public ResponseEntity<List<AdminReporteGeneralDTO>> getFacultades() {
        return ResponseEntity.ok(adminReporteService.reporteFacultades());
    }

    @GetMapping("/carreras")
    public ResponseEntity<List<AdminReporteGeneralDTO>> getCarreras(@RequestParam(required = false) Integer idFacultad) {
        return ResponseEntity.ok(adminReporteService.reporteCarreras(idFacultad));
    }

    @GetMapping("/asignaturas")
    public ResponseEntity<List<AdminReporteGeneralDTO>> getAsignaturas(@RequestParam(required = false) Integer idCarrera) {
        return ResponseEntity.ok(adminReporteService.reporteAsignaturas(idCarrera));
    }

    @GetMapping("/convocatorias")
    public ResponseEntity<List<AdminReporteGeneralDTO>> getConvocatorias(
            @RequestParam(required = false) Integer idAsignatura,
            @RequestParam(required = false) Integer idPeriodo) {
        return ResponseEntity.ok(adminReporteService.reporteConvocatorias(idAsignatura, idPeriodo));
    }

    @GetMapping("/personal")
    public ResponseEntity<List<AdminReporteGeneralDTO>> getPersonal(
            @RequestParam(required = false) Integer idFacultad,
            @RequestParam(required = false) Integer idCarrera,
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(adminReporteService.reporteDecanosCoordinadores(idFacultad, idCarrera, tipo));
    }

    @GetMapping("/postulantes")
    public ResponseEntity<List<CoordinadorPostulanteReporteDTO>> getPostulantes(
            @RequestParam(required = false) Integer idAsignatura,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(adminReporteService.reportePostulantesGlobal(idAsignatura, idPeriodo, estado));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioReporteDTO>> getUsuarios(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(adminReporteService.reporteUsuarios(rol, activo));
    }
}
