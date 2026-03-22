package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.InformeMensualRequest;
import org.uteq.sgacfinal.dto.response.InformeMensualResponse;
import org.uteq.sgacfinal.service.InformeMensualService;

import java.util.List;

@RestController
@RequestMapping("/api/informes")
@RequiredArgsConstructor
public class InformeMensualController {

    private final InformeMensualService informeMensualService;

    @GetMapping("/mis-informes")
    @PreAuthorize("hasAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<List<InformeMensualResponse>> listarMisInformes(@RequestParam Integer idUsuario) {
        return ResponseEntity.ok(informeMensualService.listarMisInformes(idUsuario));
    }

    @GetMapping("/ayudantia/{idAyudantia}")
    public ResponseEntity<List<InformeMensualResponse>> listarPorAyudantia(@PathVariable Integer idAyudantia) {
        return ResponseEntity.ok(informeMensualService.listarInformesPorAyudantia(idAyudantia));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InformeMensualResponse> detalleInforme(@PathVariable Integer id) {
        return ResponseEntity.ok(informeMensualService.obtenerDetalle(id));
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<InformeMensualResponse> generarBorradorIA(
            @RequestParam Integer idUsuario,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.ok(informeMensualService.generarBorradorConIAUsuario(idUsuario, mes, anio));
    }

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<InformeMensualResponse> enviarARevision(
            @PathVariable Integer id,
            @RequestBody InformeMensualRequest request) {
        return ResponseEntity.ok(informeMensualService.enviarARevision(id, request));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<InformeMensualResponse> aprobarInforme(
            @PathVariable Integer id,
            @RequestParam String rol) {
        return ResponseEntity.ok(informeMensualService.aprobarInforme(id, rol));
    }

    @PostMapping("/{id}/observar")
    public ResponseEntity<InformeMensualResponse> observarInforme(
            @PathVariable Integer id,
            @RequestBody InformeMensualRequest request) {
        return ResponseEntity.ok(informeMensualService.observarInforme(id, request.getObservaciones()));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<InformeMensualResponse> rechazarInforme(
            @PathVariable Integer id,
            @RequestBody InformeMensualRequest request) {
        return ResponseEntity.ok(informeMensualService.rechazarInforme(id, request.getObservaciones()));
    }

    @GetMapping("/docente/{idDocente}/pendientes")
    @PreAuthorize("hasAuthority('DOCENTE')")
    public ResponseEntity<List<InformeMensualResponse>> listarInformesPendientesDocente(
            @PathVariable Integer idDocente) {
        return ResponseEntity.ok(informeMensualService.listarInformesPorDocenteYEstado(idDocente, "EN_REVISION_DOCENTE"));
    }

    @GetMapping("/coordinador/pendientes")
    @PreAuthorize("hasAuthority('COORDINADOR')")
    public ResponseEntity<List<InformeMensualResponse>> listarPendientesCoordinador() {
        return ResponseEntity.ok(informeMensualService.listarInformesPorEstado("EN_REVISION_COORDINADOR"));
    }

    @GetMapping("/decano/pendientes")
    @PreAuthorize("hasAuthority('DECANO')")
    public ResponseEntity<List<InformeMensualResponse>> listarPendientesDecano() {
        return ResponseEntity.ok(informeMensualService.listarInformesPorEstado("EN_REVISION_DECANO"));
    }
}
