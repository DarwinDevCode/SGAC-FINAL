package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.service.IPermisoService;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final IPermisoService permisoService;

    @GetMapping
    public ResponseEntity<List<PermisoDTO>> permisosActuales() {
        return ResponseEntity.ok(permisoService.obtenerPermisos());
    }
}
