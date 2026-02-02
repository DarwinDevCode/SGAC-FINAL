package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.LoginRequestDTO;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.UsuarioDTO;
import org.uteq.sgacfinal.service.IAuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public UsuarioDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(
                request.getUsuario(),
                request.getPassword()
        );
    }

    @PostMapping("/registro-estudiante")
    public ResponseEntity<?> registrarEstudiante(@Valid @RequestBody RegistroEstudianteRequestDTO request) {
        return ResponseEntity.ok(authService.registrarEstudiante(request));
    }

    @PostMapping("/registro-docente")
    public ResponseEntity<?> registrarDocente(@Valid @RequestBody RegistroDocenteRequestDTO request) {
        return ResponseEntity.ok(authService.registrarDocente(request));
    }

    @PostMapping("/registro-coordinador")
    public ResponseEntity<?> registrarCoordinador(@Valid @RequestBody RegistroCoordinadorRequestDTO request) {
        return ResponseEntity.ok(authService.registrarCoordinador(request));
    }

    @PostMapping("/registro-decano")
    public ResponseEntity<?> registrarDecano(@Valid @RequestBody RegistroDecanoRequestDTO request) {
        return ResponseEntity.ok(authService.registrarDecano(request));
    }

    @PostMapping("/registrar-ayudante-catedra")
    public ResponseEntity<?> registrarAyudanteCatedra(@Valid @RequestBody RegistroAyudanteCatedraRequestDTO request) {
        return ResponseEntity.ok(authService.registrarAyudanteCatedra(request));
    }







}
