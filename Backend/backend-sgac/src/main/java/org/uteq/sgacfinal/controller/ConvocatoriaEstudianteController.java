package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.ConvocatoriaEstudianteResponseDTO;
import org.uteq.sgacfinal.dto.response.ConvocatoriasEstudianteWrapperDTO;
import org.uteq.sgacfinal.dto.response.ValidacionContextoEstudianteDTO;
import org.uteq.sgacfinal.dto.response.ValidacionElegibilidadAcademicaDTO;
import org.uteq.sgacfinal.service.IConvocatoriaEstudianteService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/estudiante/convocatorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ConvocatoriaEstudianteController {

    private final IConvocatoriaEstudianteService convocatoriaEstudianteService;

    @GetMapping
    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    public ResponseEntity<ConvocatoriasEstudianteWrapperDTO> listarMisConvocatorias() {
        log.info("GET /api/estudiante/convocatorias - Listando convocatorias elegibles");

        ConvocatoriasEstudianteWrapperDTO response = convocatoriaEstudianteService
                .listarMisConvocatoriasElegibles();

        if (response.getExito())
            return ResponseEntity.ok(response);
        else
            return ResponseEntity.ok(response);
    }


    @GetMapping("/listar/{idUsuario}")
    @PreAuthorize("hasAnyAuthority('ESTUDIANTE', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<ConvocatoriaEstudianteResponseDTO>> listarConvocatoriasPorUsuario(
            @PathVariable Integer idUsuario) {
        log.info("GET /api/estudiante/convocatorias/listar/{} - Listando convocatorias", idUsuario);

        List<ConvocatoriaEstudianteResponseDTO> convocatorias = convocatoriaEstudianteService
                .listarConvocatoriasElegibles(idUsuario);

        return ResponseEntity.ok(convocatorias);
    }


    @GetMapping("/validar-contexto/{idUsuario}")
    @PreAuthorize("hasAnyAuthority('ESTUDIANTE', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ValidacionContextoEstudianteDTO> validarContexto(
            @PathVariable Integer idUsuario) {
        log.info("GET /api/estudiante/convocatorias/validar-contexto/{}", idUsuario);

        ValidacionContextoEstudianteDTO validacion = convocatoriaEstudianteService
                .validarContextoEstudiante(idUsuario);

        return ResponseEntity.ok(validacion);
    }

    @GetMapping("/validar-elegibilidad/{idEstudiante}")
    @PreAuthorize("hasAnyAuthority('ESTUDIANTE', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ValidacionElegibilidadAcademicaDTO> validarElegibilidad(
            @PathVariable Integer idEstudiante) {
        log.info("GET /api/estudiante/convocatorias/validar-elegibilidad/{}", idEstudiante);

        ValidacionElegibilidadAcademicaDTO validacion = convocatoriaEstudianteService
                .verificarElegibilidadAcademica(idEstudiante);

        return ResponseEntity.ok(validacion);
    }
}

