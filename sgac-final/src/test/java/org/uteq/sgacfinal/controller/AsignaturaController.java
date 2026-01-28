package org.uteq.sgacfinal.controller;

import org.uteq.sgacfinal.dto.AsignaturaDTO;
import org.uteq.sgacfinal.dto.AsignaturaRequest;
import org.uteq.sgacfinal.service.AsignaturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    @GetMapping
    public ResponseEntity<List<AsignaturaDTO>> findAll() {
        return ResponseEntity.ok(asignaturaService.findAll());
    }

    @GetMapping("/carrera/{idCarrera}")
    public ResponseEntity<List<AsignaturaDTO>> findByCarrera(@PathVariable Integer idCarrera) {
        return ResponseEntity.ok(asignaturaService.findByCarrera(idCarrera));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsignaturaDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(asignaturaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AsignaturaDTO> create(@Valid @RequestBody AsignaturaRequest request) {
        return new ResponseEntity<>(asignaturaService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsignaturaDTO> update(@PathVariable Integer id, @Valid @RequestBody AsignaturaRequest request) {
        return ResponseEntity.ok(asignaturaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        asignaturaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
