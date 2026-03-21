package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.dto.response.DocenteDashboardDTO;
import org.uteq.sgacfinal.service.DocenteDashboardService;

@RestController
@RequestMapping("/api/docente/dashboard")
@RequiredArgsConstructor
public class DocenteDashboardController {

    private final DocenteDashboardService docenteDashboardService;

    @GetMapping("/resumen")
    @PreAuthorize("hasAuthority('DOCENTE')")
    public ResponseEntity<DocenteDashboardDTO> resumen() {
        return ResponseEntity.ok(docenteDashboardService.obtenerResumen());
    }
}
