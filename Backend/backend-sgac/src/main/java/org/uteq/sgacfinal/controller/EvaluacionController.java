package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.EvaluacionMeritosRequestDTO;
import org.uteq.sgacfinal.dto.Request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.service.IEvaluacionMeritosService;
import org.uteq.sgacfinal.service.IEvaluacionOposicionService;

@RestController
@RequestMapping("/api/evaluaciones")
@RequiredArgsConstructor
public class EvaluacionController {

    private final IEvaluacionMeritosService evaluacionMeritosService;
    private final IEvaluacionOposicionService evaluacionOposicionService;

    @PostMapping("/meritos")
    public ResponseEntity<?> registrarMeritos(@RequestBody EvaluacionMeritosRequestDTO request) {
        try {
            return ResponseEntity.ok(evaluacionMeritosService.crear(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar evaluación de méritos: " + e.getMessage());
        }
    }

    @PostMapping("/oposicion")
    public ResponseEntity<?> registrarOposicion(@RequestBody EvaluacionOposicionRequestDTO request) {
        try {
            return ResponseEntity.ok(evaluacionOposicionService.crear(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar evaluación de oposición: " + e.getMessage());
        }
    }

    @GetMapping("/meritos/postulacion/{idPostulacion}")
    public ResponseEntity<?> obtenerMeritosPorPostulacion(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(evaluacionMeritosService.buscarPorPostulacion(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/oposicion/postulacion/{idPostulacion}")
    public ResponseEntity<?> obtenerOposicionPorPostulacion(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(evaluacionOposicionService.buscarPorPostulacion(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
