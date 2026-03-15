package org.uteq.sgacfinal.controller.resultados;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.resultados.IRankingService;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@Slf4j
public class RankingController {
    private final IRankingService svc;
    private final ObjectMapper    objectMapper;

    @GetMapping("/resultados")
    public ResponseEntity<String> obtenerResultados(
            Authentication authentication,
            @RequestHeader(value = "X-Active-Role", required = false) String rolSesion // <--- Recibimos el rol activo
    ) {
        Integer idUsuario = null;
        String rolFinal = "ESTUDIANTE";

        if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            idUsuario = principal.getIdUsuario();

            List<String> rolesPermitidos = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            if (rolSesion != null && rolesPermitidos.contains(rolSesion))
                rolFinal = rolSesion;
            else
                rolFinal = rolesPermitidos.get(0);
        }

        if (idUsuario == null) return ResponseEntity.status(401).build();

        log.info("Ejecutando Ranking - Usuario: {} | Rol Activo: {}", idUsuario, rolFinal);
        JsonNode resultado = svc.obtenerRankingResultados(idUsuario, rolFinal);

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(resultado));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Error al serializar la respuesta\"}");
        }
    }
}
