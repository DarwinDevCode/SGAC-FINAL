package org.uteq.sgacfinal.controller;

import org.uteq.sgacfinal.dto.EstudianteDTO;
import org.uteq.sgacfinal.dto.EstudianteRequest;
import org.uteq.sgacfinal.service.EstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EstudianteController {

    private final EstudianteService estudianteService;

    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> findAll() {
        return ResponseEntity.ok(estudianteService.findAll());
    }

    @GetMapping("/carrera/{idCarrera}")
    public ResponseEntity<List<EstudianteDTO>> findByCarrera(@PathVariable Integer idCarrera) {
        return ResponseEntity.ok(estudianteService.findByCarrera(idCarrera));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(estudianteService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EstudianteDTO> create(@Valid @RequestBody EstudianteRequest request) {
        return new ResponseEntity<>(estudianteService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteDTO> update(@PathVariable Integer id, @Valid @RequestBody EstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        estudianteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
