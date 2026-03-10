package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.PrivilegioFuncionResponseDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoAyudantiaResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoPostulacionResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRegistroResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRequisitoResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEvidenciaResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoFaseResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoSancionAyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.service.catalogo.*;

import java.util.List;

/**
 * Controlador REST para los catálogos maestros del sistema.
 * Utiliza las funciones PostgreSQL con control transaccional.
 */
@RestController
@RequestMapping("/api/admin/catalogos-maestros")
@RequiredArgsConstructor
public class CatalogoMaestroController {

    private final ITipoSancionCatalogoService tipoSancionService;
    private final ITipoEstadoAyudantiaCatalogoService tipoEstadoAyudantiaService;
    private final ITipoEstadoRegistroCatalogoService tipoEstadoRegistroService;
    private final ITipoEvidenciaCatalogoService tipoEvidenciaService;
    private final ITipoEstadoEvidenciaCatalogoService tipoEstadoEvidenciaService;
    private final ITipoEstadoRequisitoCatalogoService tipoEstadoRequisitoService;
    private final ITipoFaseCatalogoService tipoFaseService;
    private final ITipoEstadoPostulacionCatalogoService tipoEstadoPostulacionService;
    private final IPrivilegioCatalogoService privilegioService;

    // ==================== TIPO SANCION AYUDANTE CATEDRA ====================

    @GetMapping("/tipos-sancion")
    public ResponseEntity<StandardResponseDTO<List<TipoSancionAyudanteCatedraResponseDTO>>> listarTiposSancion() {
        return ResponseEntity.ok(tipoSancionService.listar());
    }

    @PostMapping("/tipos-sancion")
    public ResponseEntity<StandardResponseDTO<Integer>> crearTipoSancion(
            @Valid @RequestBody TipoSancionAyudanteCatedraRequestDTO request) {
        return ResponseEntity.ok(tipoSancionService.crear(request));
    }

    @PutMapping("/tipos-sancion/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarTipoSancion(
            @PathVariable Integer id,
            @Valid @RequestBody TipoSancionAyudanteCatedraRequestDTO request) {
        return ResponseEntity.ok(tipoSancionService.actualizar(id, request));
    }

    @DeleteMapping("/tipos-sancion/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarTipoSancion(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoSancionService.desactivar(id));
    }

    // ==================== TIPO ESTADO AYUDANTIA ====================

    @GetMapping("/estados-ayudantia")
    public ResponseEntity<StandardResponseDTO<List<TipoEstadoAyudantiaResponseDTO>>> listarEstadosAyudantia() {
        return ResponseEntity.ok(tipoEstadoAyudantiaService.listar());
    }

    @PostMapping("/estados-ayudantia")
    public ResponseEntity<StandardResponseDTO<Integer>> crearEstadoAyudantia(
            @Valid @RequestBody TipoEstadoAyudantiaRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoAyudantiaService.crear(request));
    }

    @PutMapping("/estados-ayudantia/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarEstadoAyudantia(
            @PathVariable Integer id,
            @Valid @RequestBody TipoEstadoAyudantiaRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoAyudantiaService.actualizar(id, request));
    }

    @DeleteMapping("/estados-ayudantia/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarEstadoAyudantia(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoEstadoAyudantiaService.desactivar(id));
    }

    // ==================== TIPO ESTADO REGISTRO ====================

    @GetMapping("/estados-registro")
    public ResponseEntity<StandardResponseDTO<List<TipoEstadoRegistroResponseDTO>>> listarEstadosRegistro() {
        return ResponseEntity.ok(tipoEstadoRegistroService.listar());
    }

    @PostMapping("/estados-registro")
    public ResponseEntity<StandardResponseDTO<Integer>> crearEstadoRegistro(
            @Valid @RequestBody TipoEstadoRegistroRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoRegistroService.crear(request));
    }

    @PutMapping("/estados-registro/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarEstadoRegistro(
            @PathVariable Integer id,
            @Valid @RequestBody TipoEstadoRegistroRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoRegistroService.actualizar(id, request));
    }

    @DeleteMapping("/estados-registro/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarEstadoRegistro(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoEstadoRegistroService.desactivar(id));
    }

    // ==================== TIPO EVIDENCIA ====================

    @GetMapping("/tipos-evidencia")
    public ResponseEntity<StandardResponseDTO<List<TipoEvidenciaResponseDTO>>> listarTiposEvidencia() {
        return ResponseEntity.ok(tipoEvidenciaService.listar());
    }

    @PostMapping("/tipos-evidencia")
    public ResponseEntity<StandardResponseDTO<Integer>> crearTipoEvidencia(
            @Valid @RequestBody TipoEvidenciaRequestDTO request) {
        return ResponseEntity.ok(tipoEvidenciaService.crear(request));
    }

    @PutMapping("/tipos-evidencia/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarTipoEvidencia(
            @PathVariable Integer id,
            @Valid @RequestBody TipoEvidenciaRequestDTO request) {
        return ResponseEntity.ok(tipoEvidenciaService.actualizar(id, request));
    }

    @DeleteMapping("/tipos-evidencia/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarTipoEvidencia(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoEvidenciaService.desactivar(id));
    }

    // ==================== TIPO ESTADO EVIDENCIA ====================

    @GetMapping("/estados-evidencia")
    public ResponseEntity<StandardResponseDTO<List<TipoEstadoEvidenciaResponseDTO>>> listarEstadosEvidencia() {
        return ResponseEntity.ok(tipoEstadoEvidenciaService.listar());
    }

    @PostMapping("/estados-evidencia")
    public ResponseEntity<StandardResponseDTO<Integer>> crearEstadoEvidencia(
            @Valid @RequestBody TipoEstadoEvidenciaRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoEvidenciaService.crear(request));
    }

    @PutMapping("/estados-evidencia/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarEstadoEvidencia(
            @PathVariable Integer id,
            @Valid @RequestBody TipoEstadoEvidenciaRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoEvidenciaService.actualizar(id, request));
    }

    @DeleteMapping("/estados-evidencia/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarEstadoEvidencia(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoEstadoEvidenciaService.desactivar(id));
    }

    // ==================== TIPO ESTADO REQUISITO ====================

    @GetMapping("/estados-requisito")
    public ResponseEntity<StandardResponseDTO<List<TipoEstadoRequisitoResponseDTO>>> listarEstadosRequisito() {
        return ResponseEntity.ok(tipoEstadoRequisitoService.listar());
    }

    @PostMapping("/estados-requisito")
    public ResponseEntity<StandardResponseDTO<Integer>> crearEstadoRequisito(
            @Valid @RequestBody TipoEstadoRequisitoRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoRequisitoService.crear(request));
    }

    @PutMapping("/estados-requisito/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarEstadoRequisito(
            @PathVariable Integer id,
            @Valid @RequestBody TipoEstadoRequisitoRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoRequisitoService.actualizar(id, request));
    }

    @DeleteMapping("/estados-requisito/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarEstadoRequisito(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoEstadoRequisitoService.desactivar(id));
    }

    // ==================== TIPO FASE ====================

    @GetMapping("/tipos-fase")
    public ResponseEntity<StandardResponseDTO<List<TipoFaseResponseDTO>>> listarTiposFase() {
        return ResponseEntity.ok(tipoFaseService.listar());
    }

    @PostMapping("/tipos-fase")
    public ResponseEntity<StandardResponseDTO<Integer>> crearTipoFase(
            @Valid @RequestBody TipoFaseRequestDTO request) {
        return ResponseEntity.ok(tipoFaseService.crear(request));
    }

    @PutMapping("/tipos-fase/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarTipoFase(
            @PathVariable Integer id,
            @Valid @RequestBody TipoFaseRequestDTO request) {
        return ResponseEntity.ok(tipoFaseService.actualizar(id, request));
    }

    @DeleteMapping("/tipos-fase/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarTipoFase(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoFaseService.desactivar(id));
    }

    // ==================== TIPO ESTADO POSTULACION ====================

    @GetMapping("/estados-postulacion")
    public ResponseEntity<StandardResponseDTO<List<TipoEstadoPostulacionResponseDTO>>> listarEstadosPostulacion() {
        return ResponseEntity.ok(tipoEstadoPostulacionService.listar());
    }

    @PostMapping("/estados-postulacion")
    public ResponseEntity<StandardResponseDTO<Integer>> crearEstadoPostulacion(
            @Valid @RequestBody TipoEstadoPostulacionRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoPostulacionService.crear(request));
    }

    @PutMapping("/estados-postulacion/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarEstadoPostulacion(
            @PathVariable Integer id,
            @Valid @RequestBody TipoEstadoPostulacionRequestDTO request) {
        return ResponseEntity.ok(tipoEstadoPostulacionService.actualizar(id, request));
    }

    @DeleteMapping("/estados-postulacion/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarEstadoPostulacion(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoEstadoPostulacionService.desactivar(id));
    }

    // ==================== PRIVILEGIO ====================

    @GetMapping("/privilegios")
    public ResponseEntity<StandardResponseDTO<List<PrivilegioFuncionResponseDTO>>> listarPrivilegios() {
        return ResponseEntity.ok(privilegioService.listar());
    }

    @PostMapping("/privilegios")
    public ResponseEntity<StandardResponseDTO<Integer>> crearPrivilegio(
            @Valid @RequestBody PrivilegioRequestDTO request) {
        return ResponseEntity.ok(privilegioService.crear(request));
    }

    @PutMapping("/privilegios/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> actualizarPrivilegio(
            @PathVariable Integer id,
            @Valid @RequestBody PrivilegioRequestDTO request) {
        return ResponseEntity.ok(privilegioService.actualizar(id, request));
    }

    @DeleteMapping("/privilegios/{id}")
    public ResponseEntity<StandardResponseDTO<Integer>> desactivarPrivilegio(@PathVariable Integer id) {
        return ResponseEntity.ok(privilegioService.desactivar(id));
    }
}

