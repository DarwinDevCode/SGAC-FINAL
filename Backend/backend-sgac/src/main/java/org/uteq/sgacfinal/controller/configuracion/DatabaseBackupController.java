package org.uteq.sgacfinal.controller.configuracion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.service.impl.configuracion.DatabaseBackupService;
import java.util.List;

@RestController
@RequestMapping("/api/configuracion/respaldos")
@RequiredArgsConstructor
public class DatabaseBackupController {
    private final DatabaseBackupService backupService;

    @GetMapping
    public ResponseEntity<List<String>> listarRespaldos() {
        return ResponseEntity.ok(backupService.listarRespaldos());
    }

    @PostMapping("/generar")
    public ResponseEntity<RespuestaOperacionDTO<String>> generarRespaldo() {
        RespuestaOperacionDTO<String> respuesta = backupService.generarRespaldo();
        if (respuesta.valido()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.internalServerError().body(respuesta);
        }
    }

    @PostMapping("/restaurar/{nombreArchivo}")
    public ResponseEntity<RespuestaOperacionDTO<Void>> restaurarRespaldo(@PathVariable String nombreArchivo) {
        RespuestaOperacionDTO<Void> respuesta = backupService.restaurarRespaldo(nombreArchivo);
        if (respuesta.valido()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.internalServerError().body(respuesta);
        }
    }
}