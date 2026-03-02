package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Response.NotificacionResponseDTO;
import org.uteq.sgacfinal.service.INotificacionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificacionController {

    private final INotificacionService notificacionService;

    @GetMapping("/mis-notificaciones/{idUsuario}")
    public ResponseEntity<?> listarMisNotificaciones(@PathVariable Integer idUsuario) {
        try {
            List<NotificacionResponseDTO> response = notificacionService
                    .listarPorUsuario(idUsuario)
                    .stream()
                    .map(n -> NotificacionResponseDTO.builder()
                            .idNotificacion(n.getIdNotificacion())
                            .idUsuario(n.getUsuarioDestino() != null
                                    ? n.getUsuarioDestino().getIdUsuario()
                                    : idUsuario)
                            .mensaje(n.getMensaje())
                            .fechaEnvio(n.getFechaEnvio())
                            .leida(Boolean.TRUE.equals(n.getLeido()))
                            .tipo(n.getTipo())
                            .tipoNotificacion(n.getTipoNotificacion())
                            .idConvocatoria(n.getIdConvocatoria())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar notificaciones: " + e.getMessage());
        }
    }

    @PutMapping("/marcar-leida/{id}")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Integer id) {
        try {
            notificacionService.marcarcomoLeida(id);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Notificación marcada como leída."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
