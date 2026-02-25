package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoResponseDTO;
import org.uteq.sgacfinal.service.IPeriodoAcademicoService;

import java.util.List;

@RestController
@RequestMapping("/api/periodos-academicos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
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
}
