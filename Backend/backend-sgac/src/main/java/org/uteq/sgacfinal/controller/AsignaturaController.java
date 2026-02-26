package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.AsignaturaRequestDTO;
import org.uteq.sgacfinal.dto.Response.AsignaturaResponseDTO;
import org.uteq.sgacfinal.service.IAsignaturaService;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AsignaturaController {

    private final IAsignaturaService asignaturaService;

    @GetMapping
    public ResponseEntity<List<AsignaturaResponseDTO>> findAll() {
        return ResponseEntity.ok(asignaturaService.listarTodas());
    }

    @GetMapping("/carrera/{idCarrera}")
    public ResponseEntity<List<AsignaturaResponseDTO>> findByCarrera(@PathVariable Integer idCarrera) {
        return ResponseEntity.ok(asignaturaService.listarPorCarrera(idCarrera));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsignaturaResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(asignaturaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AsignaturaResponseDTO> create(@Valid @RequestBody AsignaturaRequestDTO request) {
        return new ResponseEntity<>(asignaturaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsignaturaResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody AsignaturaRequestDTO request) {
        return ResponseEntity.ok(asignaturaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        asignaturaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
