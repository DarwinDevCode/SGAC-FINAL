package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Response.AyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.service.IAyudanteCatedraService;

import java.util.List;

@RestController
@RequestMapping("/api/ayudantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AyudanteCatedraController {

    private final IAyudanteCatedraService ayudanteService;

    @GetMapping("/listar")
    public ResponseEntity<List<AyudanteCatedraResponseDTO>> listarTodos() {
        try {
            return ResponseEntity.ok(ayudanteService.listarTodos());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
