package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipos-rol")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TipoRolController {
//
//    private final ITipoRolService tipoRolService;
//
//    @GetMapping
//    public ResponseEntity<List<TipoRolDTO>> findAll() {
//        return ResponseEntity.ok(tipoRolService.findAll());
//    }
//
//    @GetMapping("/activos")
//    public ResponseEntity<List<TipoRolDTO>> findAllActive() {
//        return ResponseEntity.ok(tipoRolService.findAllActive());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<TipoRolDTO> findById(@PathVariable Integer id) {
//        return ResponseEntity.ok(tipoRolService.findById(id));
//    }
//
//    @PostMapping
//    public ResponseEntity<TipoRolDTO> create(@Valid @RequestBody TipoRolRequest request) {
//        TipoRolDTO created = tipoRolService.create(request);
//        return new ResponseEntity<>(created, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<TipoRolDTO> update(@PathVariable Integer id, @Valid @RequestBody TipoRolRequest request) {
//        return ResponseEntity.ok(tipoRolService.update(id, request));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Integer id) {
//        tipoRolService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PatchMapping("/{id}/toggle-active")
//    public ResponseEntity<Void> toggleActive(@PathVariable Integer id) {
//        tipoRolService.toggleActive(id);
//        return ResponseEntity.ok().build();
//    }
}
