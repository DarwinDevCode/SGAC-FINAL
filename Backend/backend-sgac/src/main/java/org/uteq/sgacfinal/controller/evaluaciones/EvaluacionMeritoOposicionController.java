package org.uteq.sgacfinal.controller.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.evaluaciones.BancoTemasRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.CambiarEstadoEvaluacionRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.PuntajeJuradoRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.SorteoOposicionRequest;
import org.uteq.sgacfinal.service.evaluaciones.IEvaluacionOposicionService;

@RestController
@RequestMapping("/api/oposicion")
@RequiredArgsConstructor
public class EvaluacionMeritoOposicionController {
    private final IEvaluacionOposicionService service;

    @PostMapping("/temas")
    public ResponseEntity<JsonNode> gestionarTemas(
            @Valid @RequestBody BancoTemasRequest request) {
        return ResponseEntity.ok(service.gestionarBancoTemas(request));
    }

    @PostMapping("/sorteo")
    public ResponseEntity<JsonNode> ejecutarSorteo(
            @Valid @RequestBody SorteoOposicionRequest request) {
        return ResponseEntity.ok(service.ejecutarSorteo(request));
    }

    @PatchMapping("/estado")
    public ResponseEntity<JsonNode> cambiarEstado(
            @Valid @RequestBody CambiarEstadoEvaluacionRequest request) {
        return ResponseEntity.ok(service.cambiarEstadoEvaluacion(request));
    }

    @PostMapping("/puntaje")
    public ResponseEntity<JsonNode> registrarPuntaje(
            @Valid @RequestBody PuntajeJuradoRequest request) {
        return ResponseEntity.ok(service.registrarPuntajeJurado(request));
    }

    @GetMapping("/cronograma/{idConvocatoria}")
    public ResponseEntity<JsonNode> obtenerCronograma(
            @PathVariable Integer idConvocatoria) {
        return ResponseEntity.ok(service.consultarCronograma(idConvocatoria));
    }
}