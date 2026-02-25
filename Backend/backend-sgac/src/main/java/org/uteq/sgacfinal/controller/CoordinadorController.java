package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.CoordinadorRequestDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorResponseDTO;
import org.uteq.sgacfinal.service.ICoordinadorService;

import java.util.List;

@RestController
@RequestMapping("/api/coordinadores")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CoordinadorController {

    private final ICoordinadorService coordinadorService;

    @GetMapping
    public ResponseEntity<List<CoordinadorResponseDTO>> findAll() {
        return ResponseEntity.ok(coordinadorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoordinadorResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(coordinadorService.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<CoordinadorResponseDTO> findByUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(coordinadorService.buscarPorUsuario(idUsuario));
    }

    @PostMapping
    public ResponseEntity<CoordinadorResponseDTO> create(@Valid @RequestBody CoordinadorRequestDTO request) {
        return new ResponseEntity<>(coordinadorService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoordinadorResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody CoordinadorRequestDTO request) {
        return ResponseEntity.ok(coordinadorService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        coordinadorService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
