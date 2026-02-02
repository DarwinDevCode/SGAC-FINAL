package org.uteq.sgacfinal.controller;

import org.uteq.sgacfinal.dto.PostulacionDTO;
import org.uteq.sgacfinal.dto.PostulacionRequest;
import org.uteq.sgacfinal.service.IPostulacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PostulacionController {

//    private final IPostulacionService postulacionService;
//
//    @GetMapping
//    public ResponseEntity<List<PostulacionDTO>> findAll() {
//        return ResponseEntity.ok(postulacionService.findAll());
//    }
//
//    @GetMapping("/activas")
//    public ResponseEntity<List<PostulacionDTO>> findByActivo() {
//        return ResponseEntity.ok(postulacionService.findByActivo(true));
//    }
//
//    @GetMapping("/convocatoria/{idConvocatoria}")
//    public ResponseEntity<List<PostulacionDTO>> findByConvocatoria(@PathVariable Integer idConvocatoria) {
//        return ResponseEntity.ok(postulacionService.findByConvocatoria(idConvocatoria));
//    }
//
//    @GetMapping("/estudiante/{idEstudiante}")
//    public ResponseEntity<List<PostulacionDTO>> findByEstudiante(@PathVariable Integer idEstudiante) {
//        return ResponseEntity.ok(postulacionService.findByEstudiante(idEstudiante));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<PostulacionDTO> findById(@PathVariable Integer id) {
//        return ResponseEntity.ok(postulacionService.findById(id));
//    }
//
//    @PostMapping
//    public ResponseEntity<PostulacionDTO> create(@Valid @RequestBody PostulacionRequest request) {
//        return new ResponseEntity<>(postulacionService.create(request), HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<PostulacionDTO> update(@PathVariable Integer id, @Valid @RequestBody PostulacionRequest request) {
//        return ResponseEntity.ok(postulacionService.update(id, request));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Integer id) {
//        postulacionService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}
