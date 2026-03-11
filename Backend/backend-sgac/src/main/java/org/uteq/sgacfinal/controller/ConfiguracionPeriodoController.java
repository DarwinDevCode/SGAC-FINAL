package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.configuracion.AjusteCronogramaRequestDTO;
import org.uteq.sgacfinal.dto.Request.configuracion.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.CronogramaActivoResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.PeriodoFaseResponseDTO;
import org.uteq.sgacfinal.service.configuracion.ICronogramaService;
import org.uteq.sgacfinal.service.configuracion.IPeriodoAcademicoService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/configuracion")
@RequiredArgsConstructor
public class ConfiguracionPeriodoController {

    private final IPeriodoAcademicoService periodoService;
    private final ICronogramaService cronogramaService;

    @PostMapping("/periodos/abrir")
    public ResponseEntity<StandardResponseDTO<Integer>> abrirPeriodo(
            @Valid @RequestBody PeriodoAcademicoRequestDTO request) {
        return ResponseEntity.ok(periodoService.abrirPeriodo(request));
    }

    @GetMapping("/cronograma/{idPeriodo}")
    public ResponseEntity<StandardResponseDTO<List<PeriodoFaseResponseDTO>>> obtenerCronograma(
            @PathVariable Integer idPeriodo) {
        return ResponseEntity.ok(cronogramaService.listarCronograma(idPeriodo));
    }

    @PostMapping("/cronograma/guardar")
    public ResponseEntity<StandardResponseDTO<Integer>> guardarCronograma(
            @Valid @RequestBody AjusteCronogramaRequestDTO request) {
        return ResponseEntity.ok(cronogramaService.guardarCronograma(request));
    }

    @PostMapping("/periodos/{id}/iniciar")
    public ResponseEntity<StandardResponseDTO<Integer>> iniciarPeriodo(
            @PathVariable Integer id) {
        return ResponseEntity.ok(periodoService.iniciarPeriodo(id));
    }

    @GetMapping("/actual")
    public ResponseEntity<CronogramaActivoResponseDTO> obtenerActual() {
        return ResponseEntity.ok(cronogramaService.obtenerCronogramaActivo());
    }
}