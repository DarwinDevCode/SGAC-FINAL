package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.service.EvaluacionDesempenoService;

@RestController
@RequestMapping("/api/evaluaciones-desempeno")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluacionDesempenoController {

    private final EvaluacionDesempenoService service;

    @PostMapping("/evaluar")
    public ResponseEntity<?> evaluar(@RequestParam Integer idRegistroActividad, 
                                    @RequestParam Integer idDocente, 
                                    @RequestParam Integer puntaje, 
                                    @RequestBody String retroalimentacion) {
        try {
            return ResponseEntity.ok(service.evaluarSesion(idRegistroActividad, idDocente, puntaje, retroalimentacion));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
