package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.FacultadRequestDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import org.uteq.sgacfinal.service.IFacultadService;

import java.util.List;

@RestController
@RequestMapping("/api/facultades")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FacultadController {

    private final IFacultadService facultadService;

    @GetMapping
    public ResponseEntity<List<FacultadResponseDTO>> findAll() {
        return ResponseEntity.ok(facultadService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultadResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(facultadService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<FacultadResponseDTO> create(@Valid @RequestBody FacultadRequestDTO request) {
        return new ResponseEntity<>(facultadService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultadResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody FacultadRequestDTO request) {
        return ResponseEntity.ok(facultadService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        facultadService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
