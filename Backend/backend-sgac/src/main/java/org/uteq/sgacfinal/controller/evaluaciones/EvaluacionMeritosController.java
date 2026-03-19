package org.uteq.sgacfinal.controller.evaluaciones;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.evaluaciones.GuardarMeritosRequest;
import org.uteq.sgacfinal.exception.AccesoDenegadoException;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.evaluaciones.IEvaluacionMeritosService;

@RestController
@RequestMapping("/api/evaluacion-meritos")
@RequiredArgsConstructor
public class EvaluacionMeritosController {
    private final IEvaluacionMeritosService service;
    private final ObjectMapper              objectMapper;

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

    private Integer idUsuario(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal p)
            return p.getIdUsuario();
        return null;
    }

    @GetMapping("/lista")
    public ResponseEntity<String> listar(Authentication auth) {
        Integer id = idUsuario(auth);
        if (id == null) return ResponseEntity.status(401).build();
        return json(service.listarPostulacionesParaMeritos(id));
    }

    @GetMapping("/postulacion/{idPostulacion}")
    public ResponseEntity<String> obtener(
            @PathVariable Integer idPostulacion,
            Authentication auth) {
        Integer id = idUsuario(auth);
        if (id == null) return ResponseEntity.status(401).build();
        return json(service.obtenerEvaluacionMeritos(idPostulacion, id));
    }

    @PostMapping
    public ResponseEntity<String> guardar(
            @Valid @RequestBody GuardarMeritosRequest request,
            Authentication auth) {
        Integer id = idUsuario(auth);
        if (id == null) return ResponseEntity.status(401).build();
        return json(service.guardarEvaluacionMeritos(request, id));
    }

    @PatchMapping("/{idPostulacion}/reabrir")
    public ResponseEntity<String> reabrir(
            @PathVariable Integer idPostulacion,
            Authentication auth) {
        Integer id = idUsuario(auth);
        if (id == null) return ResponseEntity.status(401).build();
        return json(service.reabrirEvaluacionMeritos(idPostulacion, id));
    }
}