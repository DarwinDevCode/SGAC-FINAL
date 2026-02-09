package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinadores")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CoordinadorController {

//    private final ICoordinadorService coordinadorService;
//
//    @GetMapping
//    public ResponseEntity<List<CoordinadorDTO>> findAll() {
//        return ResponseEntity.ok(coordinadorService.findAll());
//    }
//
//    @GetMapping("/activos")
//    public ResponseEntity<List<CoordinadorDTO>> findByActivo() {
//        return ResponseEntity.ok(coordinadorService.findByActivo(true));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<CoordinadorDTO> findById(@PathVariable Integer id) {
//        return ResponseEntity.ok(coordinadorService.findById(id));
//    }
//
//    @PostMapping
//    public ResponseEntity<CoordinadorDTO> create(@Valid @RequestBody CoordinadorRequest request) {
//        return new ResponseEntity<>(coordinadorService.create(request), HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<CoordinadorDTO> update(@PathVariable Integer id, @Valid @RequestBody CoordinadorRequest request) {
//        return ResponseEntity.ok(coordinadorService.update(id, request));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Integer id) {
//        coordinadorService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}
