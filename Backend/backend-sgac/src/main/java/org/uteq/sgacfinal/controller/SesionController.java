package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.RegistrarSesionRequest;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.service.SesionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {
    private final SesionService sesionService;

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

    @PostMapping
    public ResponseEntity<RegistrarSesionResponse> registrarSesion(
            @RequestParam Integer idUsuario,
            @RequestBody @Valid RegistrarSesionRequest request) {
        RegistrarSesionResponse response = sesionService.registrarSesion(idUsuario, request);
        if (!response.getExito())
            return ResponseEntity.badRequest().body(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}