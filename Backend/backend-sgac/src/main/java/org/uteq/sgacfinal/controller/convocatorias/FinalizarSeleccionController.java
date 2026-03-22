package org.uteq.sgacfinal.controller.convocatorias;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.controller.resultados.RankingController;
import org.uteq.sgacfinal.dto.Response.convocatorias.FinalizarSeleccionResponseDTO;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.impl.convocatorias.FinalizarSeleccionService;
import org.uteq.sgacfinal.util.ExtraerAuth;

@Slf4j
@RestController
@RequestMapping("/api/convocatorias")
@RequiredArgsConstructor
public class FinalizarSeleccionController {
    private final FinalizarSeleccionService service;
    private final ExtraerAuth extraerAuth;

    @PostMapping("/{idConvocatoria}/finalizar")
    @PreAuthorize("hasAuthority('COORDINADOR')")
    public ResponseEntity<FinalizarSeleccionResponseDTO> finalizar(
            @PathVariable Integer idConvocatoria,
            Authentication authentication,
            @RequestHeader(value = "X-Active-Role", required = false) String rolSesion
    ) {
        ExtraerAuth.ExtraidoAuth ea = extraerAuth.extraer(authentication, rolSesion);
        log.info("[FinalizarSeleccion] Usuario: {} ({}) solicita cierre de convocatoria: {}",
                ea.nombreUsuario(), ea.idUsuario(), idConvocatoria);

        FinalizarSeleccionResponseDTO result = service.finalizar(idConvocatoria);
        log.info("[FinalizarSeleccion] Resultado Convocatoria {}: Seleccionados={}, Elegibles={}, NoSeleccionados={}",
                idConvocatoria,
                result.getSeleccionados(),
                result.getElegibles(),
                result.getNoSeleccionados());

        return ResponseEntity.ok(result);
    }
}