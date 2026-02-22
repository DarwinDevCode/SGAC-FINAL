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
import org.uteq.sgacfinal.dto.Request.TipoEstadoEvidenciaAyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Request.TipoEstadoRequisitoRequestDTO;
import org.uteq.sgacfinal.dto.Request.TipoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Request.TipoRolRequestDTO;
import org.uteq.sgacfinal.dto.Request.TipoSancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaAyudantiaResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRequisitoResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoRequisitoPostulacionResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoSancionAyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.service.ITipoEstadoEvidenciaAyudantiaService;
import org.uteq.sgacfinal.service.ITipoEstadoRequisitoService;
import org.uteq.sgacfinal.service.ITipoRequisitoPostulacionService;
import org.uteq.sgacfinal.service.ITipoRolService;
import org.uteq.sgacfinal.service.ITipoSancionAyudanteCatedraService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final IFacultadService facultadService;
    private final ICarreraService carreraService;
    private final IAsignaturaService asignaturaService;
    private final IPeriodoAcademicoService periodoAcademicoService;

    private final ITipoEstadoEvidenciaAyudantiaService tipoEstadoEvidenciaService;
    private final ITipoEstadoRequisitoService tipoEstadoRequisitoService;
    private final ITipoRequisitoPostulacionService tipoRequisitoPostulacionService;
    private final ITipoRolService tipoRolService;
    private final ITipoSancionAyudanteCatedraService tipoSancionService;


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

    // Tipo Estado Evidencia Ayudantia
    @GetMapping("/estados-evidencia")
    public ResponseEntity<List<TipoEstadoEvidenciaAyudantiaResponseDTO>> listarEstadosEvidencia() {
        return ResponseEntity.ok(tipoEstadoEvidenciaService.listarTodos());
    }

    @PostMapping("/estados-evidencia")
    public ResponseEntity<TipoEstadoEvidenciaAyudantiaResponseDTO> crearEstadoEvidencia(@Valid @RequestBody TipoEstadoEvidenciaAyudantiaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoEstadoEvidenciaService.crear(request));
    }

    @PutMapping("/estados-evidencia/{id}")
    public ResponseEntity<TipoEstadoEvidenciaAyudantiaResponseDTO> actualizarEstadoEvidencia(@PathVariable Integer id,
                                                                                             @Valid @RequestBody TipoEstadoEvidenciaAyudantiaRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoEvidenciaService.actualizar(id, request));
    }

    @PatchMapping("/estados-evidencia/{id}/desactivar")
    public ResponseEntity<Void> desactivarEstadoEvidencia(@PathVariable Integer id) {
        tipoEstadoEvidenciaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Tipo Estado Requisito
    @GetMapping("/estados-requisito")
    public ResponseEntity<List<TipoEstadoRequisitoResponseDTO>> listarEstadosRequisito() {
        return ResponseEntity.ok(tipoEstadoRequisitoService.listarTodos());
    }

    @PostMapping("/estados-requisito")
    public ResponseEntity<TipoEstadoRequisitoResponseDTO> crearEstadoRequisito(@Valid @RequestBody TipoEstadoRequisitoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoEstadoRequisitoService.crear(request));
    }

    @PutMapping("/estados-requisito/{id}")
    public ResponseEntity<TipoEstadoRequisitoResponseDTO> actualizarEstadoRequisito(@PathVariable Integer id,
                                                                                    @Valid @RequestBody TipoEstadoRequisitoRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoRequisitoService.actualizar(id, request));
    }

    @PatchMapping("/estados-requisito/{id}/desactivar")
    public ResponseEntity<Void> desactivarEstadoRequisito(@PathVariable Integer id) {
        tipoEstadoRequisitoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Tipo Requisito Postulacion
    @GetMapping("/tipos-requisito")
    public ResponseEntity<List<TipoRequisitoPostulacionResponseDTO>> listarTiposRequisito() {
        return ResponseEntity.ok(tipoRequisitoPostulacionService.listarTodos());
    }

    @PostMapping("/tipos-requisito")
    public ResponseEntity<TipoRequisitoPostulacionResponseDTO> crearTipoRequisito(@Valid @RequestBody TipoRequisitoPostulacionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoRequisitoPostulacionService.crear(request));
    }

    @PutMapping("/tipos-requisito/{id}")
    public ResponseEntity<TipoRequisitoPostulacionResponseDTO> actualizarTipoRequisito(@PathVariable Integer id,
                                                                                       @Valid @RequestBody TipoRequisitoPostulacionRequestDTO request) {
        return ResponseEntity.ok(tipoRequisitoPostulacionService.actualizar(id, request));
    }

    @PatchMapping("/tipos-requisito/{id}/desactivar")
    public ResponseEntity<Void> desactivarTipoRequisito(@PathVariable Integer id) {
        tipoRequisitoPostulacionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Tipo Rol
    @GetMapping("/tipos-rol")
    public ResponseEntity<List<TipoRolResponseDTO>> listarTiposRol() {
        return ResponseEntity.ok(tipoRolService.listarTodos());
    }

    @PostMapping("/tipos-rol")
    public ResponseEntity<TipoRolResponseDTO> crearTipoRol(@Valid @RequestBody TipoRolRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoRolService.crear(request));
    }

    @PutMapping("/tipos-rol/{id}")
    public ResponseEntity<TipoRolResponseDTO> actualizarTipoRol(@PathVariable Integer id,
                                                                @Valid @RequestBody TipoRolRequestDTO request) {
        return ResponseEntity.ok(tipoRolService.actualizar(id, request));
    }

    @PatchMapping("/tipos-rol/{id}/desactivar")
    public ResponseEntity<Void> desactivarTipoRol(@PathVariable Integer id) {
        tipoRolService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // Tipo Sancion Ayudante Catedra
    @GetMapping("/tipos-sancion")
    public ResponseEntity<List<TipoSancionAyudanteCatedraResponseDTO>> listarTiposSancion() {
        return ResponseEntity.ok(tipoSancionService.listarTodos());
    }

    @PostMapping("/tipos-sancion")
    public ResponseEntity<TipoSancionAyudanteCatedraResponseDTO> crearTipoSancion(@Valid @RequestBody TipoSancionAyudanteCatedraRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoSancionService.crear(request));
    }

    @PutMapping("/tipos-sancion/{id}")
    public ResponseEntity<TipoSancionAyudanteCatedraResponseDTO> actualizarTipoSancion(@PathVariable Integer id,
                                                                                       @Valid @RequestBody TipoSancionAyudanteCatedraRequestDTO request) {
        return ResponseEntity.ok(tipoSancionService.actualizar(id, request));
    }

    @PatchMapping("/tipos-sancion/{id}/desactivar")
    public ResponseEntity<Void> desactivarTipoSancion(@PathVariable Integer id) {
        tipoSancionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}