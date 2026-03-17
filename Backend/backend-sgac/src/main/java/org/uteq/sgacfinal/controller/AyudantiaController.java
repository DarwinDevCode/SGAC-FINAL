package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Response.AyudantiaResponseDTO;
import org.uteq.sgacfinal.service.IAyudantiaService;

import java.util.List;

@RestController
@RequestMapping("/api/ayudantias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AyudantiaController {

    private final IAyudantiaService ayudantiaService;

    @GetMapping("/activa/{idUsuario}")
    public ResponseEntity<?> obtenerIdAyudantiaActiva(@PathVariable Integer idUsuario) {
        return ayudantiaService.buscarIdAyudantiaActivaPorUsuario(idUsuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<?> obtenerHistorialEstudiante(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(ayudantiaService.listarHistorialEstudiante(idUsuario));
    }

    @GetMapping
    public ResponseEntity<List<AyudantiaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ayudantiaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AyudantiaResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ayudantiaService.buscarPorId(id));
    }
}
