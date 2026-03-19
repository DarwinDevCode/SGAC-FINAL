package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.EvaluarActividadRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.ActividadDetalleDTO;
import org.uteq.sgacfinal.dto.response.AyudanteResponseDTO;
import org.uteq.sgacfinal.service.IDocenteMisAyudantesService;

import java.util.List;

@RestController
@RequestMapping("/api/docente")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCENTE')")
public class DocenteMisAyudantesController {

    private final IDocenteMisAyudantesService docenteMisAyudantesService;

    @GetMapping("/mis-ayudantes")
    public ResponseEntity<List<AyudanteResponseDTO>> listarMisAyudantes() {
        return ResponseEntity.ok(docenteMisAyudantesService.listarMisAyudantes());
    }

    @GetMapping("/ayudante/{idAyudantia}/actividades")
    public ResponseEntity<List<ActividadDetalleDTO>> listarActividades(@PathVariable Integer idAyudantia) {
        return ResponseEntity.ok(docenteMisAyudantesService.listarActividadesPorAyudantia(idAyudantia));
    }

    @PutMapping("/actividad/{idActividad}/evaluar")
    public ResponseEntity<Void> evaluarActividad(@PathVariable Integer idActividad,
                                                 @Valid @RequestBody EvaluarActividadRequestDTO request) {
        docenteMisAyudantesService.evaluarActividad(idActividad, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/evidencia/{idEvidencia}/evaluar")
    public ResponseEntity<Void> evaluarEvidencia(@PathVariable Integer idEvidencia,
                                                 @Valid @RequestBody EvaluarEvidenciaRequestDTO request) {
        docenteMisAyudantesService.evaluarEvidencia(idEvidencia, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/evidencia/{idEvidencia}/download")
    public ResponseEntity<Resource> descargarEvidencia(@PathVariable Integer idEvidencia) {
        return docenteMisAyudantesService.descargarEvidencia(idEvidencia);
    }
}

