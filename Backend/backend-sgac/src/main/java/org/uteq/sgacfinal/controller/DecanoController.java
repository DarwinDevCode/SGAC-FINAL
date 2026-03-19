package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.Response.DecanoResponseDTO;
import org.uteq.sgacfinal.service.IDecanoService;

import org.uteq.sgacfinal.dto.Response.DecanoEstadisticasDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorPostulanteReporteDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaDTO;
import java.util.List;

@RestController
@RequestMapping("/api/decanos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DecanoController {

    private final IDecanoService decanoService;

    @GetMapping("/facultad/{idFacultad}/estadisticas")
    public ResponseEntity<DecanoEstadisticasDTO> getEstadisticas(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(decanoService.obtenerEstadisticasPorFacultad(idFacultad));
    }

    @GetMapping("/facultad/{idFacultad}/reportes")
    public ResponseEntity<List<ConvocatoriaReporteDTO>> getReportes(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(decanoService.reporteConvocatoriasPorFacultad(idFacultad));
    }

    @GetMapping("/facultad/{idFacultad}/reportes/postulantes")
    public ResponseEntity<List<CoordinadorPostulanteReporteDTO>> getReportePostulantes(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(decanoService.reportePostulantesPorFacultad(idFacultad));
    }

    @GetMapping("/facultad/{idFacultad}/auditoria")
    public ResponseEntity<List<LogAuditoriaDTO>> getAuditoria(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(decanoService.reporteAuditoriaPorFacultad(idFacultad));
    }

    @GetMapping
    public ResponseEntity<List<DecanoResponseDTO>> findAll() {
        return ResponseEntity.ok(decanoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DecanoResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(decanoService.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<DecanoResponseDTO> findByUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(decanoService.buscarPorUsuario(idUsuario));
    }

    @PostMapping
    public ResponseEntity<DecanoResponseDTO> create(@Valid @RequestBody DecanoRequestDTO request) {
        return new ResponseEntity<>(decanoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DecanoResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody DecanoRequestDTO request) {
        return ResponseEntity.ok(decanoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        decanoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
