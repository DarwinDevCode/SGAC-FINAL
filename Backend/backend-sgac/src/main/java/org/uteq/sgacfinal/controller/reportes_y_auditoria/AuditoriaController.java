package org.uteq.sgacfinal.controller.reportes_y_auditoria;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaKpiDTO;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaResponseDTO;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.EvolucionAuditoriaProjection;
import org.uteq.sgacfinal.service.reportes_y_auditoria.AuditoriaService;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    @GetMapping("/listar")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<Page<AuditoriaResponseDTO>> listarAuditorias(
            @RequestParam(required = false) String tabla,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @PageableDefault(size = 10, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<AuditoriaResponseDTO> resultados = auditoriaService.buscarAuditorias(
                tabla, accion, idUsuario, fechaInicio, fechaFin, pageable
        );

        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/evolucion")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<List<EvolucionAuditoriaProjection>> obtenerEvolucion() {
        return ResponseEntity.ok(auditoriaService.obtenerEvolucion());
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<AuditoriaKpiDTO> obtenerKpis() {
        return ResponseEntity.ok(auditoriaService.obtenerKpis());
    }
}