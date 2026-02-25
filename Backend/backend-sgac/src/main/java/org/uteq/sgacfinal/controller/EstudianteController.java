package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.EstudianteRequestDTO;
import org.uteq.sgacfinal.dto.Response.EstudianteResponseDTO;
import org.uteq.sgacfinal.service.IEstudianteService;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EstudianteController {

    private final IEstudianteService estudianteService;

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(estudianteService.buscarPorId(id));
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<EstudianteResponseDTO> findByMatricula(@PathVariable String matricula) {
        return ResponseEntity.ok(estudianteService.buscarPorMatricula(matricula));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<EstudianteResponseDTO> findByUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(estudianteService.buscarPorUsuario(idUsuario));
    }

    @PostMapping
    public ResponseEntity<EstudianteResponseDTO> create(@Valid @RequestBody EstudianteRequestDTO request) {
        return new ResponseEntity<>(estudianteService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody EstudianteRequestDTO request) {
        return ResponseEntity.ok(estudianteService.actualizar(id, request));
    }
}
