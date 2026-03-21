package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.CompletarSesionRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarSesionRequest;
import org.uteq.sgacfinal.dto.request.PlanificarSesionRequest;
import org.uteq.sgacfinal.dto.response.SesionDTO;
import org.uteq.sgacfinal.dto.response.ProgresoGeneralResponse;
import org.uteq.sgacfinal.dto.response.ControlSemanalResponse;
import org.uteq.sgacfinal.dto.response.CompletarSesionResponseDTO;
import org.uteq.sgacfinal.dto.response.EvaluarSesionResponse;
import org.uteq.sgacfinal.dto.response.PlanificarSesionResponseDTO;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.ayudantia.SesionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SesionConfigController {
    private final SesionService sesionService;

    @PostMapping("/planificar")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<PlanificarSesionResponseDTO> planificar(
            @Valid @RequestBody PlanificarSesionRequest request,
            Authentication auth) {




        Integer idUsuario = extraerIdUsuario(auth);
        return ResponseEntity.ok(sesionService.planificarSesion(idUsuario, request));
    }

    @PostMapping(
            value    = "/{idRegistro}/completar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<CompletarSesionResponseDTO> completar(
            @PathVariable Integer idRegistro,
            @RequestPart("datos") @Valid CompletarSesionRequestDTO request,
            @RequestPart(value = "archivos", required = false)
            List<MultipartFile> archivos,
            Authentication auth) {

        Integer idUsuario = extraerIdUsuario(auth);
        return ResponseEntity.ok(
                sesionService.completarSesion(
                        idUsuario,
                        idRegistro,
                        request,
                        archivos != null ? archivos : List.of()
                )
        );
    }

    @PutMapping("/{idRegistro}/evaluar")
    @PreAuthorize("hasAnyAuthority('DOCENTE', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EvaluarSesionResponse> evaluar(
            @PathVariable Integer idRegistro,
            @Valid @RequestBody EvaluarSesionRequest request,
            Authentication auth) {

        Integer idUsuario = extraerIdUsuario(auth);
        return ResponseEntity.ok(
                sesionService.evaluarSesion(idUsuario, idRegistro, request));
    }

    @GetMapping("/mis-sesiones")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'DOCENTE', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<List<SesionDTO>> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estadoCodigo,
            @RequestParam(required = false) Integer idAyudantia,
            Authentication auth) {

        Integer idUsuario = extraerIdUsuario(auth);
        return ResponseEntity.ok(
                sesionService.listarSesiones(
                        idUsuario, fechaDesde, fechaHasta, estadoCodigo, idAyudantia));
    }

    @GetMapping("/{idRegistro}")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'DOCENTE', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<SesionDTO> detalle(
            @PathVariable Integer idRegistro,
            Authentication auth) {

        Integer idUsuario = extraerIdUsuario(auth);
        return ResponseEntity.ok(sesionService.detalleSesion(idUsuario, idRegistro));
    }

    @GetMapping("/progreso")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<ProgresoGeneralResponse> progresoGeneral(
            @RequestParam Integer idUsuario) {
        return ResponseEntity.ok(sesionService.progresoGeneral(idUsuario));
    }

    @GetMapping("/control-semanal")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<ControlSemanalResponse> controlSemanal(
            @RequestParam Integer idUsuario) {
        return ResponseEntity.ok(sesionService.controlSemanal(idUsuario));
    }

    private Integer extraerIdUsuario(Authentication auth) {
        return ((UsuarioPrincipal) auth.getPrincipal()).getIdUsuario();
    }
}