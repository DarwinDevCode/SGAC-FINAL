package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.service.INotificacionService;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificacionController {

    private final INotificacionService notificacionService;

    @GetMapping("/mis-notificaciones/{idUsuario}")
    public ResponseEntity<?> listarMisNotificaciones(@PathVariable Integer idUsuario) {
        try {
            return ResponseEntity.ok(notificacionService.listarPorUsuario(idUsuario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al listar notificaciones: " + e.getMessage());
        }
    }

    @PutMapping("/marcar-leida/{id}")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Integer id) {
        try {
            notificacionService.marcarcomoLeida(id);
            return ResponseEntity.ok("Notificación marcada como leída.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
