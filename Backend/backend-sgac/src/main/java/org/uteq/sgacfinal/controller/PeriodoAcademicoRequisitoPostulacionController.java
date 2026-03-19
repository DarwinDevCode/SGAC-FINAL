package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.PeriodoAcademicoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.response.PeriodoAcademicoRequisitoPostulacionResponseDTO;
import org.uteq.sgacfinal.service.IPeriodoAcademicoRequisitoPostulacionService;

import java.util.Map;

@RestController
@RequestMapping("/api/periodos-requisitos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PeriodoAcademicoRequisitoPostulacionController {

    private final IPeriodoAcademicoRequisitoPostulacionService service;

    @PostMapping
    public ResponseEntity<PeriodoAcademicoRequisitoPostulacionResponseDTO> crear(
            @RequestBody PeriodoAcademicoRequisitoPostulacionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeriodoAcademicoRequisitoPostulacionResponseDTO> actualizar(
            @PathVariable Integer id,
            @RequestBody PeriodoAcademicoRequisitoPostulacionRequestDTO request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ítem 5: importa los requisitos ACTIVOS de un período origen al período destino.
     * POST /api/periodos-requisitos/importar?idOrigen=X&idDestino=Y
     *
     * @return cantidad de requisitos importados
     */
    @PostMapping("/importar")
    public ResponseEntity<?> importarRequisitos(
            @RequestParam Integer idOrigen,
            @RequestParam Integer idDestino) {
        try {
            int cantidad = service.importarDeOtroPeriodo(idOrigen, idDestino);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Se importaron " + cantidad + " requisito(s) correctamente.",
                    "cantidadImportada", cantidad
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al importar requisitos: " + e.getMessage()));
        }
    }
}
