package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.service.InformeMensualService;
import java.util.List;

@RestController
@RequestMapping("/api/informes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class InformeMensualController {

    private final InformeMensualService service;

    @PostMapping("/generar")
    public ResponseEntity<?> generar(@RequestParam Integer idAyudantia, @RequestParam Integer mes, @RequestParam Integer anio) {
        try {
            return ResponseEntity.ok(service.generarInforme(idAyudantia, mes, anio));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Lista todos los informes de una ayudantía (para el Ayudante) */
    @GetMapping("/ayudantia/{idAyudantia}")
    public ResponseEntity<?> listarPorAyudantia(@PathVariable Integer idAyudantia) {
        return ResponseEntity.ok(service.listarPorAyudantia(idAyudantia));
    }

    /** Lista los informes pendientes de revisión asignados al docente */
    @GetMapping("/docente/{idDocente}/pendientes")
    public ResponseEntity<?> listarPendientesDocente(@PathVariable Integer idDocente) {
        return ResponseEntity.ok(service.listarPendientesDocente(idDocente));
    }

    /** Lista los informes revisados por el docente, pendientes de aprobación del coordinador */
    @GetMapping("/coordinador/{idCoordinador}/pendientes")
    public ResponseEntity<?> listarPendientesCoordinador(@PathVariable Integer idCoordinador) {
        return ResponseEntity.ok(service.listarPendientesCoordinador(idCoordinador));
    }

    @PostMapping("/revisar-docente/{idInforme}")
    public ResponseEntity<?> revisarDocente(@PathVariable Integer idInforme, @RequestBody String observaciones) {
        try {
            return ResponseEntity.ok(service.revisionDocente(idInforme, observaciones));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/rechazar-docente/{idInforme}")
    public ResponseEntity<?> rechazarDocente(@PathVariable Integer idInforme, @RequestBody String observaciones) {
        try {
            return ResponseEntity.ok(service.rechazarDocente(idInforme, observaciones));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/aprobar-coordinador/{idInforme}")
    public ResponseEntity<?> aprobarCoordinador(@PathVariable Integer idInforme) {
        try {
            return ResponseEntity.ok(service.aprobacionCoordinador(idInforme));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/rechazar-coordinador/{idInforme}")
    public ResponseEntity<?> rechazarCoordinador(@PathVariable Integer idInforme, @RequestBody String observaciones) {
        try {
            return ResponseEntity.ok(service.rechazarCoordinador(idInforme, observaciones));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
