package org.uteq.sgacfinal.controller.convocatorias;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.convocatorias.ComisionDetalleResponseDTO;
import org.uteq.sgacfinal.dto.response.convocatorias.GenerarComisionesResponseDTO;
import org.uteq.sgacfinal.service.convocatorias.IComisionService;

@RestController
@RequiredArgsConstructor
public class ComisionController {
    private final IComisionService comisionService;

    @PreAuthorize("hasAnyAuthority('COORDINADOR', 'ADMINISTRADOR')")
    @PostMapping("/api/comisiones/generar")
    public ResponseEntity<GenerarComisionesResponseDTO> generarComisiones() {
        return ResponseEntity.ok(comisionService.generarComisionesAutomaticas());
    }

    @PreAuthorize("hasAnyAuthority('COORDINADOR', 'ADMINISTRADOR', 'DECANO', 'COMISION_SELECCION')")
    @GetMapping("/api/comisiones/detalle")
    public ResponseEntity<ComisionDetalleResponseDTO> obtenerDetalle(
            @RequestParam Integer idUsuario,
            @RequestParam String  rol) {
        return ResponseEntity.ok(comisionService.consultarComision(idUsuario, rol));
    }
}