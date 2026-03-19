package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.AyudantiaDetalleResponseDTO;
import org.uteq.sgacfinal.dto.response.RegistroActividadDetalleDTO;
import org.uteq.sgacfinal.service.IAyudantiaService;

import java.util.List;

@RestController
@RequestMapping("/api/ayudantia-detalle")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AyudantiaDetalleController {

    private final IAyudantiaService ayudantiaDetalleService;

    // Endpoint para findAyudantiaConDetalles
    @GetMapping("/{idAyudantia}/completo")
    public ResponseEntity<AyudantiaDetalleResponseDTO> getDetallesCompletos(@PathVariable Integer idAyudantia) {
        return ResponseEntity.ok(ayudantiaDetalleService.obtenerDetallescompletos(idAyudantia));
    }

    // Endpoint para findActividadesRawByUsuario
    @GetMapping("/usuario/{idUsuario}/actividades")
    public ResponseEntity<List<RegistroActividadDetalleDTO>> getActividadesPorUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(ayudantiaDetalleService.listarActividadesPorUsuario(idUsuario));
    }
}