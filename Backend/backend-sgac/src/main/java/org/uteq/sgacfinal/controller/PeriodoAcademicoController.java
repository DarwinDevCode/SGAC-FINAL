package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.response.PeriodoAcademicoResponseDTO;
import org.uteq.sgacfinal.service.IPeriodoAcademicoService;

import java.util.List;

@RestController
@RequestMapping("/api/periodos-academicos")
@RequiredArgsConstructor
public class PeriodoAcademicoController {

    private final IPeriodoAcademicoService periodoAcademicoService;

    @GetMapping
    public ResponseEntity<List<PeriodoAcademicoResponseDTO>> findAll() {
        return ResponseEntity.ok(periodoAcademicoService.listarTodos());
    }

    @GetMapping("/activo")
    public ResponseEntity<PeriodoAcademicoResponseDTO> getPeriodoActivo() {
        return ResponseEntity.ok(periodoAcademicoService.obtenerPeriodoActivo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeriodoAcademicoResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(periodoAcademicoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PeriodoAcademicoResponseDTO> create(@Valid @RequestBody PeriodoAcademicoRequestDTO request) {
        return new ResponseEntity<>(periodoAcademicoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeriodoAcademicoResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody PeriodoAcademicoRequestDTO request) {
        return ResponseEntity.ok(periodoAcademicoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        periodoAcademicoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable Integer id) {
        try {
            periodoAcademicoService.activar(id);
            return ResponseEntity.ok("Período activado correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/importar-requisitos")
    public ResponseEntity<?> importarRequisitos(@PathVariable Integer id, @RequestParam Integer fuentePeriodoId) {
        try {
            int cantidad = periodoAcademicoService.importarRequisitos(id, fuentePeriodoId);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Se importaron " + cantidad + " requisitos exitosamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
