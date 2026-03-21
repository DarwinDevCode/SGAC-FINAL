package org.uteq.sgacfinal.controller.convocatorias;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.convocatorias.ComisionDetalleResponseDTO;
import org.uteq.sgacfinal.dto.response.convocatorias.GenerarComisionesResponseDTO;
import org.uteq.sgacfinal.service.convocatorias.IComisionService;

@RestController
@RequiredArgsConstructor
public class ComisionController {
    private final IComisionService comisionService;

    @PostMapping("/api/comisiones/generar")
    public ResponseEntity<GenerarComisionesResponseDTO> generarComisiones() {
        return ResponseEntity.ok(comisionService.generarComisionesAutomaticas());
    }

    @GetMapping("/api/comisiones/detalle")
    public ResponseEntity<ComisionDetalleResponseDTO> obtenerDetalle(
            @RequestParam Integer idUsuario,
            @RequestParam String  rol) {
        return ResponseEntity.ok(comisionService.consultarComision(idUsuario, rol));
    }
}