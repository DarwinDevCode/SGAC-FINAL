package org.uteq.sgacfinal.controller;

import org.uteq.sgacfinal.dto.FacultadDTO;
import org.uteq.sgacfinal.dto.FacultadRequest;
import org.uteq.sgacfinal.service.FacultadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facultades")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FacultadController {

    private final FacultadService facultadService;

    @GetMapping
    public ResponseEntity<List<FacultadDTO>> findAll() {
        return ResponseEntity.ok(facultadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultadDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(facultadService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FacultadDTO> create(@Valid @RequestBody FacultadRequest request) {
        return new ResponseEntity<>(facultadService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultadDTO> update(@PathVariable Integer id, @Valid @RequestBody FacultadRequest request) {
        return ResponseEntity.ok(facultadService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        facultadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
