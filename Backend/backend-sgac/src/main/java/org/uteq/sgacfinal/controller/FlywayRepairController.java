package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mantenimiento/flyway")
@RequiredArgsConstructor
public class FlywayRepairController {

    private final Flyway flyway;

    @PostMapping("/repair")
    public ResponseEntity<?> repairFlyway() {
        try {
            flyway.repair();
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Flyway repair ejecutado con éxito. Los checksums han sido actualizados en la base de datos.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al ejecutar flyway repair: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
