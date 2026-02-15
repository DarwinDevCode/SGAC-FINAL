package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.service.IRecursosConvocatoriaService;

import java.util.List;

@RestController
@RequestMapping("/api/recursos")
@RequiredArgsConstructor
public class RecursosConvocatoriaController {

    private final IRecursosConvocatoriaService recursosService;

    @GetMapping("/docentes")
    public ResponseEntity<List<DocenteResponseDTO>> listarDocentes() {
        return ResponseEntity.ok(recursosService.obtenerDocentesParaSelector());
    }

    @GetMapping("/asignaturas")
    public ResponseEntity<List<AsignaturaResponseDTO>> listarAsignaturas() {
        return ResponseEntity.ok(recursosService.obtenerAsignaturasParaSelector());
    }

    @GetMapping("/periodos")
    public ResponseEntity<PeriodoAcademicoResponseDTO> getPeriodoActivo() {
        return ResponseEntity.ok(recursosService.obtenerPeriodoActivo());
    }

    @GetMapping("/facultades")
    public ResponseEntity<List<FacultadResponseDTO>> listarFacultades() {
        return ResponseEntity.ok(recursosService.listarFacultades());
    }

    @GetMapping("/carreras")
    public ResponseEntity<List<CarreraResponseDTO>> listarCarreras() {
        return ResponseEntity.ok(recursosService.listarCarreras());
    }
}