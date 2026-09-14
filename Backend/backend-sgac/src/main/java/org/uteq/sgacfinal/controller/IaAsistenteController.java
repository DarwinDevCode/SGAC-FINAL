package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.IaAsistenteRequestDTO;
import org.uteq.sgacfinal.dto.response.IaAsistenteResponseDTO;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.uteq.sgacfinal.service.IIaAsistenteService;

@RestController
@RequestMapping("/api/ia-asistente")
@RequiredArgsConstructor
public class IaAsistenteController {

    private final IIaAsistenteService iaAsistenteService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/consultar")
    public ResponseEntity<IaAsistenteResponseDTO> consultar(
            @RequestBody IaAsistenteRequestDTO request,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        // En una app real, el principal siempre debe existir si está autenticado.
        Integer idUsuario = usuario != null ? usuario.getIdUsuario() : 0;
        return ResponseEntity.ok(iaAsistenteService.consultar(request, idUsuario));
    }
}
