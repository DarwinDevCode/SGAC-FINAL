package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.CarreraRequestDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import org.uteq.sgacfinal.service.ICarreraService;

import java.util.List;

@RestController
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CarreraController {

    private final ICarreraService carreraService;

    @GetMapping
    public ResponseEntity<List<CarreraResponseDTO>> findAll() {
        return ResponseEntity.ok(carreraService.listarTodas());
    }

    @GetMapping("/facultad/{idFacultad}")
    public ResponseEntity<List<CarreraResponseDTO>> findByFacultad(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(carreraService.listarPorFacultad(idFacultad));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarreraResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(carreraService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CarreraResponseDTO> create(@Valid @RequestBody CarreraRequestDTO request) {
        return new ResponseEntity<>(carreraService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarreraResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody CarreraRequestDTO request) {
        return ResponseEntity.ok(carreraService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        carreraService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
