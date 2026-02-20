package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.AsignaturaRequestDTO;
import org.uteq.sgacfinal.dto.Request.CarreraRequestDTO;
import org.uteq.sgacfinal.dto.Request.FacultadRequestDTO;
import org.uteq.sgacfinal.dto.Request.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.Response.AsignaturaResponseDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoResponseDTO;
import org.uteq.sgacfinal.service.IAsignaturaService;
import org.uteq.sgacfinal.service.ICarreraService;
import org.uteq.sgacfinal.service.IFacultadService;
import org.uteq.sgacfinal.service.IPeriodoAcademicoService;

import java.util.List;

@RestController
    @RequestMapping("/api/admin/catalogos")
@RequiredArgsConstructor
public class CatalogoAdminController {

    private final IFacultadService facultadService;
    private final ICarreraService carreraService;
    private final IAsignaturaService asignaturaService;
    private final IPeriodoAcademicoService periodoAcademicoService;

    // Facultad
    @GetMapping("/facultades")
    public ResponseEntity<List<FacultadResponseDTO>> listarFacultades() {
        return ResponseEntity.ok(facultadService.listarTodas());
    }

    @PostMapping("/facultades")
    public ResponseEntity<FacultadResponseDTO> crearFacultad(@Valid @RequestBody FacultadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultadService.crear(request));
    }

    @PutMapping("/facultades/{id}")
    public ResponseEntity<FacultadResponseDTO> actualizarFacultad(@PathVariable Integer id,
                                                                   @Valid @RequestBody FacultadRequestDTO request) {
        return ResponseEntity.ok(facultadService.actualizar(id, request));
    }

    @PatchMapping("/facultades/{id}/desactivar")
    public ResponseEntity<Void> desactivarFacultad(@PathVariable Integer id) {
        facultadService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Carrera
    @GetMapping("/carreras")
    public ResponseEntity<List<CarreraResponseDTO>> listarCarreras() {
        return ResponseEntity.ok(carreraService.listarTodas());
    }

    @PostMapping("/carreras")
    public ResponseEntity<CarreraResponseDTO> crearCarrera(@Valid @RequestBody CarreraRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraService.crear(request));
    }

    @PutMapping("/carreras/{id}")
    public ResponseEntity<CarreraResponseDTO> actualizarCarrera(@PathVariable Integer id,
                                                                 @Valid @RequestBody CarreraRequestDTO request) {
        return ResponseEntity.ok(carreraService.actualizar(id, request));
    }

    @PatchMapping("/carreras/{id}/desactivar")
    public ResponseEntity<Void> desactivarCarrera(@PathVariable Integer id) {
        carreraService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Asignatura
    @GetMapping("/asignaturas")
    public ResponseEntity<List<AsignaturaResponseDTO>> listarAsignaturas() {
        return ResponseEntity.ok(asignaturaService.listarTodas());
    }

    @PostMapping("/asignaturas")
    public ResponseEntity<AsignaturaResponseDTO> crearAsignatura(@Valid @RequestBody AsignaturaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asignaturaService.crear(request));
    }

    @PutMapping("/asignaturas/{id}")
    public ResponseEntity<AsignaturaResponseDTO> actualizarAsignatura(@PathVariable Integer id,
                                                                       @Valid @RequestBody AsignaturaRequestDTO request) {
        return ResponseEntity.ok(asignaturaService.actualizar(id, request));
    }

    @PatchMapping("/asignaturas/{id}/desactivar")
    public ResponseEntity<Void> desactivarAsignatura(@PathVariable Integer id) {
        asignaturaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Periodo academico
    @GetMapping("/periodos")
    public ResponseEntity<List<PeriodoAcademicoResponseDTO>> listarPeriodos() {
        return ResponseEntity.ok(periodoAcademicoService.listarTodos());
    }

    @PostMapping("/periodos")
    public ResponseEntity<PeriodoAcademicoResponseDTO> crearPeriodo(@Valid @RequestBody PeriodoAcademicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(periodoAcademicoService.crear(request));
    }

    @PutMapping("/periodos/{id}")
    public ResponseEntity<PeriodoAcademicoResponseDTO> actualizarPeriodo(@PathVariable Integer id,
                                                                          @Valid @RequestBody PeriodoAcademicoRequestDTO request) {
        return ResponseEntity.ok(periodoAcademicoService.actualizar(id, request));
    }

    @PatchMapping("/periodos/{id}/desactivar")
    public ResponseEntity<Void> desactivarPeriodo(@PathVariable Integer id) {
        periodoAcademicoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
