package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.dto.LoginRequest;
import org.uteq.sgacfinal.dto.LoginResultadoDTO;
import org.uteq.sgacfinal.service.IAuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResultadoDTO> login(@RequestBody LoginRequest request) {
        LoginResultadoDTO resultado =
                authService.login(request.getUsuario(), request.getPassword());
        if (resultado == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(resultado);
    }
}

