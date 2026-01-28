package com.sgac.controller;

import com.sgac.dto.UsuarioDTO;
import com.sgac.dto.UsuarioRequest;
import com.sgac.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioDTO>> findAllActive() {
        return ResponseEntity.ok(usuarioService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @GetMapping("/username/{nombreUsuario}")
    public ResponseEntity<UsuarioDTO> findByNombreUsuario(@PathVariable String nombreUsuario) {
        return ResponseEntity.ok(usuarioService.findByNombreUsuario(nombreUsuario));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> create(@Valid @RequestBody UsuarioRequest request) {
        UsuarioDTO created = usuarioService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Integer id, @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable Integer id) {
        usuarioService.toggleActive(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{usuarioId}/roles/{tipoRolId}")
    public ResponseEntity<Void> assignRole(@PathVariable Integer usuarioId, @PathVariable Integer tipoRolId) {
        usuarioService.assignRole(usuarioId, tipoRolId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{usuarioId}/roles/{tipoRolId}")
    public ResponseEntity<Void> removeRole(@PathVariable Integer usuarioId, @PathVariable Integer tipoRolId) {
        usuarioService.removeRole(usuarioId, tipoRolId);
        return ResponseEntity.noContent().build();
    }
}
