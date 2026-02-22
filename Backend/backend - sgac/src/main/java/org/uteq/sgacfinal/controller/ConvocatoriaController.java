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

    @PostMapping("/crear") // O simplemente @PostMapping si prefieres estándar REST estricto
    public ResponseEntity<ConvocatoriaResponseDTO> crear(@RequestBody ConvocatoriaRequestDTO dto) {
        return new ResponseEntity<>(convocatoriaService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/actualizar") // O @PutMapping si pasas el ID en el body
    public ResponseEntity<ConvocatoriaResponseDTO> actualizar(@RequestBody ConvocatoriaRequestDTO dto) {
        return ResponseEntity.ok(convocatoriaService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        convocatoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}













//package org.uteq.sgacfinal.controller;
//import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
//import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
//import org.uteq.sgacfinal.service.IConvocatoriaService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/convocatorias")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")
//public class ConvocatoriaController {
//    private final IConvocatoriaService convocatoriaService;
//
//    @GetMapping("/listar-vista")
//    public ResponseEntity<List<ConvocatoriaResponseDTO>> listarConvocatorias() {
//        return ResponseEntity.ok(convocatoriaService.obtenerTodasLasConvocatorias());
//    }
//
//    @PostMapping("/crear")
//    public ResponseEntity<ConvocatoriaResponseDTO> crear(@Valid @RequestBody ConvocatoriaRequestDTO request) {
//        return ResponseEntity.ok(convocatoriaService.crear(request));
//    }
//
//
////    private final IConvocatoriaService convocatoriaService;
////
////    @GetMapping
////    public ResponseEntity<List<ConvocatoriaDTO>> findAll() {
////        return ResponseEntity.ok(convocatoriaService.findAll());
////    }
////
////    @GetMapping("/activas")
////    public ResponseEntity<List<ConvocatoriaDTO>> findByActivo() {
////        return ResponseEntity.ok(convocatoriaService.findByActivo(true));
////    }
////
////    @GetMapping("/periodo/{idPeriodo}")
////    public ResponseEntity<List<ConvocatoriaDTO>> findByPeriodo(@PathVariable Integer idPeriodo) {
////        return ResponseEntity.ok(convocatoriaService.findByPeriodo(idPeriodo));
////    }
////
////    @GetMapping("/{id}")
////    public ResponseEntity<ConvocatoriaDTO> findById(@PathVariable Integer id) {
////        return ResponseEntity.ok(convocatoriaService.findById(id));
////    }
////
////    @PostMapping
////    public ResponseEntity<ConvocatoriaDTO> create(@Valid @RequestBody ConvocatoriaRequest request) {
////        return new ResponseEntity<>(convocatoriaService.create(request), HttpStatus.CREATED);
////    }
////
////    @PutMapping("/{id}")
////    public ResponseEntity<ConvocatoriaDTO> update(@PathVariable Integer id, @Valid @RequestBody ConvocatoriaRequest request) {
////        return ResponseEntity.ok(convocatoriaService.update(id, request));
////    }
////
////    @DeleteMapping("/{id}")
////    public ResponseEntity<Void> delete(@PathVariable Integer id) {
////        convocatoriaService.delete(id);
////        return ResponseEntity.noContent().build();
////    }
//}
