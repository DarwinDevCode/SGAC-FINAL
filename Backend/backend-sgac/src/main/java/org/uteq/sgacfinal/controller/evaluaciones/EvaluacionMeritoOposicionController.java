package org.uteq.sgacfinal.controller.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.evaluaciones.BancoTemasRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.CambiarEstadoEvaluacionRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.PuntajeJuradoRequest;
import org.uteq.sgacfinal.dto.Request.evaluaciones.SorteoOposicionRequest;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.evaluaciones.ConvocatoriaOposicionDTO;
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

    @PostMapping("/temas")
    public ResponseEntity<String> gestionarTemas(
            @Valid @RequestBody BancoTemasRequest request) {
        return json(service.gestionarBancoTemas(request));
    }

    @PostMapping("/sorteo")
    public ResponseEntity<String> ejecutarSorteo(
            @Valid @RequestBody SorteoOposicionRequest request) {
        return json(service.ejecutarSorteo(request));
    }

    @PatchMapping("/estado")
    public ResponseEntity<String> cambiarEstado(
            @Valid @RequestBody CambiarEstadoEvaluacionRequest request) {
        return json(service.cambiarEstadoEvaluacion(request));
    }


    @GetMapping("/cronograma/{idConvocatoria}")
    public ResponseEntity<String> obtenerCronograma(
            @PathVariable Integer idConvocatoria) {
        return json(service.consultarCronograma(idConvocatoria));
    }

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

    @GetMapping("/mi-turno/{idConvocatoria}")
    public ResponseEntity<JsonNode> obtenerMiTurno(
            @PathVariable Integer idConvocatoria,
            Authentication authentication) {

        Integer idUsuario = null;
        if (authentication != null &&
                authentication.getPrincipal() instanceof UsuarioPrincipal principal)
            idUsuario = principal.getIdUsuario();

        if (idUsuario == null)
            return ResponseEntity.status(401).build();

        return ResponseEntity.ok(service.obtenerMiTurno(idConvocatoria, idUsuario));
    }

    @GetMapping("/convocatorias-aptas")
    public ResponseEntity<StandardResponseDTO<List<ConvocatoriaOposicionDTO>>> listarConvocatoriasAptas(
            @AuthenticationPrincipal UserDetails userDetails) {

        StandardResponseDTO<List<ConvocatoriaOposicionDTO>> response =
                service.listarConvocatoriasParaOposicion();

        return ResponseEntity.ok(response);
    }
}