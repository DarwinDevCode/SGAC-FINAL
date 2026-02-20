package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodos-academicos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PeriodoAcademicoController {
//
//    private final IPeriodoAcademicoService periodoAcademicoService;
//
//    @GetMapping
//    public ResponseEntity<List<PeriodoAcademicoDTO>> findAll() {
//        return ResponseEntity.ok(periodoAcademicoService.findAll());
//    }
//
//    @GetMapping("/estado/{estado}")
//    public ResponseEntity<List<PeriodoAcademicoDTO>> findByEstado(@PathVariable String estado) {
//        return ResponseEntity.ok(periodoAcademicoService.findByEstado(estado));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<PeriodoAcademicoDTO> findById(@PathVariable Integer id) {
//        return ResponseEntity.ok(periodoAcademicoService.findById(id));
//    }
//
//    @PostMapping
//    public ResponseEntity<PeriodoAcademicoDTO> create(@Valid @RequestBody PeriodoAcademicoRequest request) {
//        return new ResponseEntity<>(periodoAcademicoService.create(request), HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<PeriodoAcademicoDTO> update(@PathVariable Integer id, @Valid @RequestBody PeriodoAcademicoRequest request) {
//        return ResponseEntity.ok(periodoAcademicoService.update(id, request));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Integer id) {
//        periodoAcademicoService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}
