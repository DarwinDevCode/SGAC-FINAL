package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.service.IConvocatoriaService;

import java.util.List;

@RestController
@RequestMapping("/api/convocatorias")
@RequiredArgsConstructor
public class ConvocatoriaController {

    private final IConvocatoriaService convocatoriaService;

    @GetMapping("/listar-vista")
    public ResponseEntity<List<ConvocatoriaResponseDTO>> listarTodo() {
        return ResponseEntity.ok(convocatoriaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConvocatoriaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.findById(id));
    }

    @PostMapping("/crear")
    public ResponseEntity<ConvocatoriaResponseDTO> crear(@RequestBody ConvocatoriaRequestDTO dto) {
        return new ResponseEntity<>(convocatoriaService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/actualizar")
    public ResponseEntity<ConvocatoriaResponseDTO> actualizar(@RequestBody ConvocatoriaRequestDTO dto) {
        return ResponseEntity.ok(convocatoriaService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        convocatoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
