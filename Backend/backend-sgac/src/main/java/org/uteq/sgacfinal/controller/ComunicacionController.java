package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.entity.MensajeInterno;
import org.uteq.sgacfinal.service.ComunicacionService;

import java.util.List;

@RestController
@RequestMapping("/api/comunicacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ComunicacionController {

    private final ComunicacionService comunicacionService;

    @GetMapping("/historial/{idAyudantia}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable Integer idAyudantia) {
        try {
            return ResponseEntity.ok(comunicacionService.obtenerHistorial(idAyudantia));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener el historial: " + e.getMessage());
        }
    }

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@RequestBody org.uteq.sgacfinal.entity.MensajeInterno mensaje) {
        try {
            return ResponseEntity.ok(comunicacionService.enviarMensaje(mensaje));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    @GetMapping("/buscar/{idAyudantia}")
    public ResponseEntity<?> buscar(@PathVariable Integer idAyudantia, @RequestParam String q) {
        return ResponseEntity.ok(comunicacionService.buscarMensajes(idAyudantia, q));
    }
}
