package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.SincronizarCargaRequest;
import org.uteq.sgacfinal.dto.response.AsignaturaJerarquiaDTO;
import org.uteq.sgacfinal.dto.response.DocenteActivoDTO;
import org.uteq.sgacfinal.dto.response.SincronizarCargaResponseDTO;
import org.uteq.sgacfinal.service.impl.CargaAcademicaService;

import java.util.List;

@RestController
@RequestMapping("/api/carga-academica")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class CargaAcademicaController {
    private final CargaAcademicaService service;

    @GetMapping("/docentes")
    public ResponseEntity<List<DocenteActivoDTO>> listarDocentes() {
        return ResponseEntity.ok(service.listarDocentes());
    }

    @GetMapping("/docentes/{idDocente}/asignaturas")
    public ResponseEntity<List<AsignaturaJerarquiaDTO>> listarAsignaturasDocente(
            @PathVariable Integer idDocente) {
        return ResponseEntity.ok(service.listarAsignaturasDocente(idDocente));
    }

    @GetMapping("/asignaturas")
    public ResponseEntity<List<AsignaturaJerarquiaDTO>> listarAsignaturas() {
        return ResponseEntity.ok(service.listarAsignaturas());
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<SincronizarCargaResponseDTO> sincronizar(
            @Valid @RequestBody SincronizarCargaRequest req) {
        return ResponseEntity.ok(service.sincronizar(req));
    }
}