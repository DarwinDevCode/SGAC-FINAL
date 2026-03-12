package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.service.IAdminConsultaService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/consulta")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AdminConsultaController {

    private final IAdminConsultaService adminConsultaService;

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            return ResponseEntity.ok(adminConsultaService.obtenerResumenSistema());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}
