package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.RegistrarSesionRequest;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.service.IAyudantiaService;
import org.uteq.sgacfinal.service.SesionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {
    private final SesionService sesionService;
    private final IAyudantiaService ayudantiaService;

    @GetMapping
    public ResponseEntity<List<SesionListadoResponse>> listarSesiones(
            @RequestParam Integer idUsuario,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idPeriodo) {
        return ResponseEntity.ok(
                sesionService.listarSesiones(idUsuario, fechaDesde, fechaHasta, estado, idPeriodo)
        );
    }

    @GetMapping("/{idRegistro}")
    public ResponseEntity<SesionDetalleResponse> detalleSesion(
            @RequestParam Integer idUsuario,
            @PathVariable Integer idRegistro) {
        return ResponseEntity.ok(sesionService.detalleSesion(idUsuario, idRegistro));
    }

    @GetMapping("/{idRegistro}/evidencias")
    public ResponseEntity<List<EvidenciaResponse>> evidenciasSesion(
            @RequestParam Integer idUsuario,
            @PathVariable Integer idRegistro) {
        return ResponseEntity.ok(sesionService.evidenciasSesion(idUsuario, idRegistro));
    }

    @GetMapping("/progreso")
    public ResponseEntity<ProgresoGeneralResponse> progresoGeneral(
            @RequestParam Integer idUsuario) {
        return ResponseEntity.ok(sesionService.progresoGeneral(idUsuario));
    }

    @GetMapping("/control-semanal")
    public ResponseEntity<ControlSemanalResponse> controlSemanal(
            @RequestParam Integer idUsuario) {
        return ResponseEntity.ok(sesionService.controlSemanal(idUsuario));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistrarSesionResponse> registrarSesion(
            @RequestParam Integer idUsuario,
            @RequestPart("request") @Valid RegistrarSesionRequest request,
            @RequestPart("archivos") List<MultipartFile> archivos) {

        RegistrarSesionResponse response = sesionService.registrarSesion(idUsuario, request, archivos);
        if (!response.getExito())
            return ResponseEntity.badRequest().body(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Módulo "Mis Sesiones" para AYUDANTE_CATEDRA.
     * Lista todas las sesiones/actividades registradas por el ayudante (idUsuario).
     */
    @GetMapping("/mis-sesiones")
    @PreAuthorize("hasAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<List<SesionResponseDTO>> listarMisSesiones(@RequestParam Integer idAyudante) {
        return ResponseEntity.ok(ayudantiaService.listarSesionesPorAyudante(idAyudante));
    }

    /**
     * Módulo "Mis Sesiones" para AYUDANTE_CATEDRA.
     * Detalle de una sesión específica (incluye evidencias).
     */
    @GetMapping("/mis-sesiones/{idRegistroActividad}")
    @PreAuthorize("hasAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<SesionResponseDTO> detalleMiSesion(
            @RequestParam Integer idAyudante,
            @PathVariable Integer idRegistroActividad) {

        return ayudantiaService.obtenerDetalleSesionConEvidencias(idAyudante, idRegistroActividad)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}