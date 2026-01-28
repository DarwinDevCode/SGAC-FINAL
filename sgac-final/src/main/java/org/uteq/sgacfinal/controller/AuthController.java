package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.LoginRequestDTO;
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
}
