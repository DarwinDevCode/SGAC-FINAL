package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.dto.Response.estudiante.EstudianteDashboardResponseDTO;
import org.uteq.sgacfinal.service.estudiante.EstudianteDashboardService;

@RestController
@RequestMapping("/api/estudiante/dashboard")
@RequiredArgsConstructor
public class EstudianteDashboardController {

    private final EstudianteDashboardService estudianteDashboardService;

    @GetMapping("/resumen")
    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    public ResponseEntity<EstudianteDashboardResponseDTO> resumen() {
        return ResponseEntity.ok(estudianteDashboardService.obtenerResumen());
    }
}