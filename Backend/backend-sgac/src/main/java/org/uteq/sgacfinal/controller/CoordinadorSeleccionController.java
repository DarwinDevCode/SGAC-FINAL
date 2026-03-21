package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.AsignaturaSelectableDTO;
import org.uteq.sgacfinal.dto.response.DocenteSelectableDTO;
import org.uteq.sgacfinal.service.ICoordinadorSeleccionService;

import java.util.List;

@RestController
@RequestMapping("/api/coordinador")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('COORDINADOR')")
public class CoordinadorSeleccionController {

    private final ICoordinadorSeleccionService seleccionService;

    /**
     * GET /api/coordinador/docentes-seleccionables
     */
    @GetMapping("/docentes-seleccionables")
    public ResponseEntity<List<DocenteSelectableDTO>> docentesSeleccionables() {
        return ResponseEntity.ok(seleccionService.listarDocentesSeleccionables());
    }

    /**
     * GET /api/coordinador/docentes/{idDocente}/asignaturas
     */
    @GetMapping("/docentes/{idDocente}/asignaturas")
    public ResponseEntity<List<AsignaturaSelectableDTO>> asignaturasPorDocente(@PathVariable Integer idDocente) {
        return ResponseEntity.ok(seleccionService.listarAsignaturasPorDocente(idDocente));
    }
}

