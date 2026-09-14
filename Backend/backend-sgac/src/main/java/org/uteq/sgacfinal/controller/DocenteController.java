package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.CambiarEstadoActividadRequest;
import org.uteq.sgacfinal.dto.request.CambiarEstadoEvidenciaRequest;
import org.uteq.sgacfinal.dto.response.AyudanteResumenDTO;
import org.uteq.sgacfinal.dto.response.DocenteDashboardDTO;
import org.uteq.sgacfinal.dto.response.DocenteResponseDTO;
import org.uteq.sgacfinal.dto.response.RegistroActividadDocenteDTO;
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
    // Lectura general (ej. dropdown "docente responsable" al crear una convocatoria)

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'COORDINADOR', 'DECANO', 'DOCENTE')")
    @GetMapping
    public ResponseEntity<List<DocenteResponseDTO>> listarDocentesActivos() {
        return ResponseEntity.ok(docenteService.listarDocentesActivos());
    }

    // ── Dashboard y gestion de los propios ayudantes: solo el docente dueno ──────

    @PreAuthorize("hasAuthority('DOCENTE')")
    @GetMapping("/dashboard")
    public ResponseEntity<DocenteDashboardDTO> getDashboard(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(
                docenteActividadesService.getDashboard(principal.getIdUsuario()));
    }

    // ── Ayudantes ───────────────────────────────────────────────────────────────

    @PreAuthorize("hasAuthority('DOCENTE')")
    @GetMapping("/ayudantes")
    public ResponseEntity<List<AyudanteResumenDTO>> listarAyudantes(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(
                docenteActividadesService.listarAyudantes(principal.getIdUsuario()));
    }

    // ── Actividades de un ayudante ──────────────────────────────────────────────

    @PreAuthorize("hasAuthority('DOCENTE')")
    @GetMapping("/ayudantes/{idAyudantia}/actividades")
    public ResponseEntity<List<RegistroActividadDocenteDTO>> getActividadesAyudante(
            @PathVariable Integer idAyudantia) {
        return ResponseEntity.ok(
                docenteActividadesService.listarActividadesAyudante(idAyudantia));
    }

    // ── Detalle de una actividad ────────────────────────────────────────────────

    @PreAuthorize("hasAuthority('DOCENTE')")
    @GetMapping("/actividades/{idActividad}")
    public ResponseEntity<RegistroActividadDocenteDTO> getDetalleActividad(
            @PathVariable Integer idActividad) {
        return ResponseEntity.ok(
                docenteActividadesService.getDetalleActividad(idActividad));
    }

    // ── Cambiar estado global de actividad ──────────────────────────────────────

    @PreAuthorize("hasAuthority('DOCENTE')")
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

    @PreAuthorize("hasAuthority('DOCENTE')")
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
