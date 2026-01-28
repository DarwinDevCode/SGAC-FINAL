package com.sgac.controller;

import com.sgac.dto.CarreraDTO;
import com.sgac.dto.CarreraRequest;
import com.sgac.service.CarreraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CarreraController {

    private final CarreraService carreraService;

    @GetMapping
    public ResponseEntity<List<CarreraDTO>> findAll() {
        return ResponseEntity.ok(carreraService.findAll());
    }

    @GetMapping("/facultad/{idFacultad}")
    public ResponseEntity<List<CarreraDTO>> findByFacultad(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(carreraService.findByFacultad(idFacultad));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarreraDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(carreraService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CarreraDTO> create(@Valid @RequestBody CarreraRequest request) {
        return new ResponseEntity<>(carreraService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarreraDTO> update(@PathVariable Integer id, @Valid @RequestBody CarreraRequest request) {
        return ResponseEntity.ok(carreraService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        carreraService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
