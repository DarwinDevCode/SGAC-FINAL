package org.uteq.sgacfinal.controller;

import org.uteq.sgacfinal.dto.ConvocatoriaDTO;
import org.uteq.sgacfinal.dto.ConvocatoriaRequest;
import org.uteq.sgacfinal.service.ConvocatoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convocatorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ConvocatoriaController {

    private final ConvocatoriaService convocatoriaService;

    @GetMapping
    public ResponseEntity<List<ConvocatoriaDTO>> findAll() {
        return ResponseEntity.ok(convocatoriaService.findAll());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<ConvocatoriaDTO>> findByActivo() {
        return ResponseEntity.ok(convocatoriaService.findByActivo(true));
    }

    @GetMapping("/periodo/{idPeriodo}")
    public ResponseEntity<List<ConvocatoriaDTO>> findByPeriodo(@PathVariable Integer idPeriodo) {
        return ResponseEntity.ok(convocatoriaService.findByPeriodo(idPeriodo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConvocatoriaDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ConvocatoriaDTO> create(@Valid @RequestBody ConvocatoriaRequest request) {
        return new ResponseEntity<>(convocatoriaService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConvocatoriaDTO> update(@PathVariable Integer id, @Valid @RequestBody ConvocatoriaRequest request) {
        return ResponseEntity.ok(convocatoriaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        convocatoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
