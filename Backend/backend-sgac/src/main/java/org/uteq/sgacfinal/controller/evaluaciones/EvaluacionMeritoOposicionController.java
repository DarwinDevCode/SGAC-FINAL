package org.uteq.sgacfinal.controller.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.evaluaciones.BancoTemasRequest;
import org.uteq.sgacfinal.dto.request.evaluaciones.CambiarEstadoEvaluacionRequest;
import org.uteq.sgacfinal.dto.request.evaluaciones.PuntajeJuradoRequest;
import org.uteq.sgacfinal.dto.request.evaluaciones.SorteoOposicionRequest;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.evaluaciones.ConvocatoriaOposicionDTO;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.evaluaciones.IEvaluacionOposicionService;

import java.util.List;

@RestController
@RequestMapping("/api/oposicion")
@RequiredArgsConstructor
public class EvaluacionMeritoOposicionController {
    private final IEvaluacionOposicionService service;
    private final ObjectMapper objectMapper;

    private ResponseEntity<String> json(JsonNode node) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Error al serializar la respuesta\"}");
        }
    }

    @PreAuthorize("hasAuthority('COORDINADOR')")
    @PostMapping("/temas")
    public ResponseEntity<String> gestionarTemas(
            @Valid @RequestBody BancoTemasRequest request) {
        return json(service.gestionarBancoTemas(request));
    }

    @PreAuthorize("hasAuthority('COORDINADOR')")
    @PostMapping("/sorteo")
    public ResponseEntity<String> ejecutarSorteo(
            @Valid @RequestBody SorteoOposicionRequest request) {
        return json(service.ejecutarSorteo(request));
    }

    @PreAuthorize("hasAnyAuthority('COORDINADOR', 'COMISION_SELECCION')")
    @PatchMapping("/estado")
    public ResponseEntity<String> cambiarEstado(
            @Valid @RequestBody CambiarEstadoEvaluacionRequest request) {
        return json(service.cambiarEstadoEvaluacion(request));
    }

    @PreAuthorize("hasAnyAuthority('COORDINADOR', 'COMISION_SELECCION', 'ESTUDIANTE')")
    @GetMapping("/cronograma/{idConvocatoria}")
    public ResponseEntity<String> obtenerCronograma(
            @PathVariable Integer idConvocatoria) {
        return json(service.consultarCronograma(idConvocatoria));
    }

    @PreAuthorize("hasAuthority('COMISION_SELECCION')")
    @PostMapping("/puntaje")
    public ResponseEntity<String> registrarPuntaje(
            @Valid @RequestBody PuntajeJuradoRequest request,
            Authentication authentication) {

        if (authentication != null &&
                authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            request.setIdUsuario(principal.getIdUsuario());
        }
        return json(service.registrarPuntajeJurado(request));
    }

    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    @GetMapping("/mi-turno/{idConvocatoria}")
    public ResponseEntity<String> obtenerMiTurno(
            @PathVariable Integer idConvocatoria,
            Authentication authentication) {

        Integer idUsuario = null;
        if (authentication != null &&
                authentication.getPrincipal() instanceof UsuarioPrincipal principal)
            idUsuario = principal.getIdUsuario();

        if (idUsuario == null)
            return ResponseEntity.status(401).build();

        // Se serializa manualmente con el ObjectMapper (Jackson 2) de este archivo,
        // igual que el resto de endpoints del controlador (ver metodo json()).
        // Devolver el JsonNode directo aqui hacia que Spring (Jackson 3) lo tratara
        // como un bean generico en vez de como un arbol JSON, exponiendo metodos
        // internos como "isArray"/"isBigDecimal" en la respuesta.
        return json(service.obtenerMiTurno(idConvocatoria, idUsuario));
    }

    @PreAuthorize("hasAnyAuthority('COORDINADOR', 'COMISION_SELECCION')")
    @GetMapping("/convocatorias-aptas")
    public ResponseEntity<StandardResponseDTO<List<ConvocatoriaOposicionDTO>>> listarConvocatoriasAptas(
            @AuthenticationPrincipal UserDetails userDetails) {

        StandardResponseDTO<List<ConvocatoriaOposicionDTO>> response =
                service.listarConvocatoriasParaOposicion();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ESTUDIANTE', 'COMISION_SELECCION')")
    @GetMapping("/mi-sala")
    public ResponseEntity<String> resolverMiSala(Authentication authentication) {
        if (authentication == null ||
                !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            return ResponseEntity.status(401).build();
        }
        return json(service.resolverSalaUsuario(principal.getIdUsuario()));
    }

    /*
    @GetMapping("/mi-sala")
    public ResponseEntity<String> resolverMiSala(Authentication authentication) {
        Integer idUsuario = null;
        if (authentication != null &&
                authentication.getPrincipal() instanceof UsuarioPrincipal principal)
            idUsuario = principal.getIdUsuario();
        if (idUsuario == null) return ResponseEntity.status(401).build();
        return json(service.resolverMiSala(idUsuario));
    }
     */
}