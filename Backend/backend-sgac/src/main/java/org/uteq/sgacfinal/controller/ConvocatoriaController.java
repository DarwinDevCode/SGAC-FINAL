package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.configuracion.ConvocatoriaActualizarRequestDTO;
import org.uteq.sgacfinal.dto.request.configuracion.ConvocatoriaCrearRequestDTO;
import org.uteq.sgacfinal.dto.response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.ConvocatoriaNativaResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.VerificarFaseResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.VerificarPostulantesResponseDTO;
import org.uteq.sgacfinal.dto.response.estudiante.ConvocatoriaEstudianteDTO;
import org.uteq.sgacfinal.service.IConvocatoriaService;

import java.util.List;

@RestController
@RequestMapping("/api/convocatorias")
@RequiredArgsConstructor
public class ConvocatoriaController {

    private final IConvocatoriaService convocatoriaService;
    
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'DECANO', 'ESTUDIANTE', 'DOCENTE', 'AYUDANTE_CATEDRA')")
    @GetMapping("/listar-vista")
    public ResponseEntity<List<ConvocatoriaResponseDTO>> listarTodo() {
        return ResponseEntity.ok(convocatoriaService.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'DECANO', 'ESTUDIANTE', 'DOCENTE', 'AYUDANTE_CATEDRA')")
    @GetMapping("/{id}")
    public ResponseEntity<ConvocatoriaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.findById(id));
    }

    @PreAuthorize("hasRole('ESTUDIANTE')")
    @GetMapping("/listar-por-estudiante/{idUsuario}")
    public ResponseEntity<List<ConvocatoriaEstudianteDTO>> listarPorEstudiante(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(convocatoriaService.listarConvocatoriasEstudiante(idUsuario));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/verificar-fase")
    public ResponseEntity<VerificarFaseResponseDTO> verificarFase() {
        return ResponseEntity.ok(convocatoriaService.verificarFase());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/check-postulantes/{id}")
    public ResponseEntity<VerificarPostulantesResponseDTO> checkPostulantes(
            @PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.checkPostulantes(id));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/guardar")
    public ResponseEntity<ConvocatoriaNativaResponseDTO> guardar(
            @Valid @RequestBody ConvocatoriaCrearRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convocatoriaService.crear(request));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/actualizar")
    public ResponseEntity<ConvocatoriaNativaResponseDTO> actualizar(
            @Valid @RequestBody ConvocatoriaActualizarRequestDTO request) {
        return ResponseEntity.ok(convocatoriaService.actualizar(request));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/desactivar/{id}")
    public ResponseEntity<ConvocatoriaNativaResponseDTO> desactivar(
            @PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.desactivar(id));
    }
}

