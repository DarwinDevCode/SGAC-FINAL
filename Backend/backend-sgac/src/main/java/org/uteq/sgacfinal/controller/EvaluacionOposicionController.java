package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.AsignarComisionRequestDTO;
import org.uteq.sgacfinal.dto.Request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.service.IEvaluacionOposicionService;

import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluacionOposicionController {

    private final IEvaluacionOposicionService evaluacionService;

    @PostMapping("/asignar")
    public ResponseEntity<?> asignarComision(@RequestBody AsignarComisionRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(evaluacionService.asignarComisionAPostulacion(request));
        } catch (Exception e) {
            e.printStackTrace();
            String causeDetails = e.getMessage();
            Throwable cause = e.getCause();
            while (cause != null) {
                causeDetails += " | Cause: " + cause.getMessage();
                cause = cause.getCause();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al asignar la comisión: " + causeDetails);
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody EvaluacionOposicionRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionService.crear(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la evaluación: " + e.getMessage());
        }
    }

    @GetMapping("/postulacion/{idPostulacion}")
    public ResponseEntity<?> buscarPorPostulacion(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(evaluacionService.buscarPorPostulacion(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró evaluación para la postulación: " + e.getMessage());
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarTodas() {
        try {
            return ResponseEntity.ok(evaluacionService.listarTodas());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar evaluaciones: " + e.getMessage());
        }
    }

    @PostMapping("/sortear/{idEvaluacionOposicion}")
    public ResponseEntity<?> sortearTema(@PathVariable Integer idEvaluacionOposicion) {
        try {
            return ResponseEntity.ok(evaluacionService.sortearTema(idEvaluacionOposicion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al sortear el tema: " + e.getMessage());
        }
    }
}
