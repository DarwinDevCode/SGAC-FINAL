package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.dto.response.configuracion.CronogramaActivoResponseDTO;
import org.uteq.sgacfinal.service.ICronogramaActivoService;

@RestController
@RequestMapping("/api/cronograma")
@RequiredArgsConstructor
public class CronogramaActivoController {

    private final ICronogramaActivoService cronogramaService;

    @GetMapping("/actual")
    public ResponseEntity<CronogramaActivoResponseDTO> obtenerActual() {
        return ResponseEntity.ok(cronogramaService.obtenerCronogramaActivo());
    }
}