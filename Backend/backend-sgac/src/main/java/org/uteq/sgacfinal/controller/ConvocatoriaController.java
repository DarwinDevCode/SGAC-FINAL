package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Request.configuracion.ConvocatoriaActualizarRequestDTO;
import org.uteq.sgacfinal.dto.Request.configuracion.ConvocatoriaCrearRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.ConvocatoriaNativaResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.VerificarFaseResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.VerificarPostulantesResponseDTO;
import org.uteq.sgacfinal.dto.Response.estudiante.ConvocatoriaEstudianteDTO;
import org.uteq.sgacfinal.repository.estudiante.IGestionConvocatoria;
import org.uteq.sgacfinal.service.IConvocatoriaService;

import java.util.List;

@RestController
@RequestMapping("/api/convocatorias")
@RequiredArgsConstructor
public class ConvocatoriaController {

    private final IConvocatoriaService convocatoriaService;
    private final IGestionConvocatoria gestionConvocatoria;

    @GetMapping("/listar-vista")
    public ResponseEntity<List<ConvocatoriaResponseDTO>> listarTodo() {
        return ResponseEntity.ok(convocatoriaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConvocatoriaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.findById(id));
    }

    //@Deprecated
    //@PutMapping("/actualizar")
    //public ResponseEntity<ConvocatoriaResponseDTO> actualizar(@RequestBody ConvocatoriaRequestDTO dto) {
    //    return ResponseEntity.ok(convocatoriaService.update(dto));
    //}


    @GetMapping("/listar-por-estudiante/{idUsuario}")
    public ResponseEntity<List<ConvocatoriaEstudianteDTO>> listarPorEstudiante(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(gestionConvocatoria.listarConvocatoriasEstudiante(idUsuario));
    }


    @GetMapping("/verificar-fase")
    public ResponseEntity<VerificarFaseResponseDTO> verificarFase() {
        return ResponseEntity.ok(convocatoriaService.verificarFase());
    }

    @GetMapping("/check-postulantes/{id}")
    public ResponseEntity<VerificarPostulantesResponseDTO> checkPostulantes(
            @PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.checkPostulantes(id));
    }

    @PostMapping("/guardar")
    public ResponseEntity<ConvocatoriaNativaResponseDTO> guardar(
            @Valid @RequestBody ConvocatoriaCrearRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convocatoriaService.crear(request));
    }

    @PutMapping("/actualizar")
    public ResponseEntity<ConvocatoriaNativaResponseDTO> actualizar(
            @Valid @RequestBody ConvocatoriaActualizarRequestDTO request) {
        return ResponseEntity.ok(convocatoriaService.actualizar(request));
    }

    @PatchMapping("/desactivar/{id}")
    public ResponseEntity<ConvocatoriaNativaResponseDTO> desactivar(
            @PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.desactivar(id));
    }




    //@Deprecated
    //@PostMapping("/crear")
    //public ResponseEntity<ConvocatoriaResponseDTO> crear(
    //        @RequestBody ConvocatoriaRequestDTO dto) {
    //    return new ResponseEntity<>(convocatoriaService.create(dto), HttpStatus.CREATED);
    //}

    //@Deprecated
    //@PutMapping("/actualizar-legacy")
    //public ResponseEntity<ConvocatoriaResponseDTO> actualizarLegacy(
    //        @RequestBody ConvocatoriaRequestDTO dto) {
    //    return ResponseEntity.ok(convocatoriaService.update(dto));
    //}

    //  @DeleteMapping("/eliminar/{id}")
   //   public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
   //      convocatoriaService.delete(id);
   //      return ResponseEntity.noContent().build();
   //   }
}
