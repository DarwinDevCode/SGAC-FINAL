package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DocenteController {
//
//    private final IDocenteService docenteService;
//
//    @GetMapping
//    public ResponseEntity<List<DocenteDTO>> findAll() {
//        return ResponseEntity.ok(docenteService.findAll());
//    }
//
//    @GetMapping("/activos")
//    public ResponseEntity<List<DocenteDTO>> findByActivo() {
//        return ResponseEntity.ok(docenteService.findByActivo(true));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<DocenteDTO> findById(@PathVariable Integer id) {
//        return ResponseEntity.ok(docenteService.findById(id));
//    }
//
//    @PostMapping
//    public ResponseEntity<DocenteDTO> create(@Valid @RequestBody DocenteRequest request) {
//        return new ResponseEntity<>(docenteService.create(request), HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<DocenteDTO> update(@PathVariable Integer id, @Valid @RequestBody DocenteRequest request) {
//        return ResponseEntity.ok(docenteService.update(id, request));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Integer id) {
//        docenteService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}
