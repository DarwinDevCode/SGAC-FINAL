package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.CambiarEstadoActividadRequest;
import org.uteq.sgacfinal.dto.Request.CambiarEstadoEvidenciaRequest;
import org.uteq.sgacfinal.dto.Response.AyudanteResumenDTO;
import org.uteq.sgacfinal.dto.Response.DocenteDashboardDTO;
import org.uteq.sgacfinal.dto.Response.DocenteResponseDTO;
import org.uteq.sgacfinal.dto.Response.RegistroActividadDocenteDTO;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.IDocenteActividadesService;
import org.uteq.sgacfinal.service.IDocenteService;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
public class DocenteController {

    private final IDocenteService docenteService;
    private final IDocenteActividadesService docenteActividadesService;

    // ── Existente ──────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<DocenteResponseDTO>> listarDocentesActivos() {
        return ResponseEntity.ok(docenteService.listarDocentesActivos());
    }

    // ── Dashboard ───────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<DocenteDashboardDTO> getDashboard(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(
                docenteActividadesService.getDashboard(principal.getIdUsuario()));
    }

    // ── Ayudantes ───────────────────────────────────────────────────────────────

    @GetMapping("/ayudantes")
    public ResponseEntity<List<AyudanteResumenDTO>> listarAyudantes(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(
                docenteActividadesService.listarAyudantes(principal.getIdUsuario()));
    }

    // ── Actividades de un ayudante ──────────────────────────────────────────────

    @GetMapping("/ayudantes/{idAyudantia}/actividades")
    public ResponseEntity<List<RegistroActividadDocenteDTO>> getActividadesAyudante(
            @PathVariable Integer idAyudantia) {
        return ResponseEntity.ok(
                docenteActividadesService.listarActividadesAyudante(idAyudantia));
    }

    // ── Detalle de una actividad ────────────────────────────────────────────────

    @GetMapping("/actividades/{idActividad}")
    public ResponseEntity<RegistroActividadDocenteDTO> getDetalleActividad(
            @PathVariable Integer idActividad) {
        return ResponseEntity.ok(
                docenteActividadesService.getDetalleActividad(idActividad));
    }

    // ── Cambiar estado global de actividad ──────────────────────────────────────

    @PutMapping("/actividades/{idActividad}/estado")
    public ResponseEntity<Void> cambiarEstadoActividad(
            @PathVariable Integer idActividad,
            @RequestBody CambiarEstadoActividadRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        docenteActividadesService.cambiarEstadoActividad(
                idActividad, request, principal.getIdUsuario());
        return ResponseEntity.ok().build();
    }

    // ── Cambiar estado de evidencia específica ──────────────────────────────────

    @PutMapping("/evidencias/{idEvidencia}/estado")
    public ResponseEntity<Void> cambiarEstadoEvidencia(
            @PathVariable Integer idEvidencia,
            @RequestBody CambiarEstadoEvidenciaRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        docenteActividadesService.cambiarEstadoEvidencia(
                idEvidencia, request, principal.getIdUsuario());
        return ResponseEntity.ok().build();
    }
}
