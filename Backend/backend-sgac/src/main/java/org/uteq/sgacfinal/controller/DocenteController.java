package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Response.DocenteResponseDTO;
import org.uteq.sgacfinal.service.IDocenteService;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
public class DocenteController {

    private final IDocenteService docenteService;

    @GetMapping
    public ResponseEntity<List<DocenteResponseDTO>> listarDocentesActivos() {
        return ResponseEntity.ok(docenteService.listarDocentesActivos());
    }
}
