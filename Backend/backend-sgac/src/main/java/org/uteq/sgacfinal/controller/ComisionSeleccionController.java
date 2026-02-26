package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.ComisionSeleccionRequestDTO;
import org.uteq.sgacfinal.dto.Response.ComisionSeleccionResponseDTO;
import org.uteq.sgacfinal.service.IComisionSeleccionService;

import java.util.List;

@RestController
@RequestMapping("/api/comisiones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ComisionSeleccionController {

    private final IComisionSeleccionService comisionService;

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody ComisionSeleccionRequestDTO request) {
        try {
            ComisionSeleccionResponseDTO resp = comisionService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la comisión: " + e.getMessage());
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody ComisionSeleccionRequestDTO request) {
        try {
            return ResponseEntity.ok(comisionService.actualizar(id, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la comisión: " + e.getMessage());
        }
    }

    @GetMapping("/convocatoria/{idConvocatoria}")
    public ResponseEntity<?> listarPorConvocatoria(@PathVariable Integer idConvocatoria) {
        try {
            List<ComisionSeleccionResponseDTO> lista = comisionService.listarPorConvocatoria(idConvocatoria);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar comisiones: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(comisionService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Comisión no encontrada: " + e.getMessage());
        }
    }

    @DeleteMapping("/desactivar/{id}")
    public ResponseEntity<?> desactivar(@PathVariable Integer id) {
        try {
            comisionService.desactivar(id);
            return ResponseEntity.ok("Comisión desactivada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al desactivar: " + e.getMessage());
        }
    }
}
