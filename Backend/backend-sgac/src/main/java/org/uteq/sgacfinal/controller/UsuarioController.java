package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.UsuarioRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.service.IUsuariosService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final IUsuariosService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }
}
