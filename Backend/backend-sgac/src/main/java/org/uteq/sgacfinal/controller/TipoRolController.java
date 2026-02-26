package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.TipoRolRequestDTO;
import org.uteq.sgacfinal.dto.Response.RolResumenResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import org.uteq.sgacfinal.service.ITipoRolService;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-rol")
@RequiredArgsConstructor
public class TipoRolController {
    private final ITipoRolService tipoRolService;

    @GetMapping("/resumen-permisos")
    public ResponseEntity<List<RolResumenResponseDTO>> obtenerRolesParaPermisos() {
        List<RolResumenResponseDTO> roles = tipoRolService.obtenerRolesParaPermisos();
        return ResponseEntity.ok(roles);
    }

    @GetMapping
    public ResponseEntity<List<TipoRolResponseDTO>> findAll() {
        return ResponseEntity.ok(tipoRolService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<TipoRolResponseDTO>> findAllActive() {
        return ResponseEntity.ok(tipoRolService.listarActivos());
    }

    @PostMapping
    public ResponseEntity<TipoRolResponseDTO> create(@Valid @RequestBody TipoRolRequestDTO request) {
        TipoRolResponseDTO created = tipoRolService.crear(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoRolResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody TipoRolRequestDTO request) {
        return ResponseEntity.ok(tipoRolService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        tipoRolService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
