package org.uteq.sgacfinal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.DictaminarPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarDocumentoRequestDTO;
import org.uteq.sgacfinal.dto.request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.response.*;
import org.uteq.sgacfinal.service.IEvaluacionPostulacionService;
import org.uteq.sgacfinal.service.ILogAuditoriaService;

import java.util.List;

/**
 * Controller para la evaluación de postulaciones por parte del coordinador
 */
@RestController
@RequestMapping("/api/coordinador/evaluacion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EvaluacionPostulacionController {

    private final IEvaluacionPostulacionService evaluacionService;
    private final ILogAuditoriaService logAuditoriaService;

    /**
     * Lista todas las postulaciones de la carrera del coordinador
     */
    @GetMapping("/postulaciones/{idUsuario}")
    public ResponseEntity<List<PostulacionListadoCoordinadorDTO>> listarPostulaciones(
            @PathVariable Integer idUsuario) {

        log.info("GET /api/coordinador/evaluacion/postulaciones/{}", idUsuario);

        try {
            List<PostulacionListadoCoordinadorDTO> postulaciones = evaluacionService.listarPostulacionesCoordinador(idUsuario);
            return ResponseEntity.ok(postulaciones);
        } catch (Exception e) {
            log.error("Error crítico al listar postulaciones para el usuario {}: {}", idUsuario, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Obtiene el detalle completo de una postulación
     */
    @GetMapping("/postulaciones/{idUsuario}/detalle/{idPostulacion}")
    public ResponseEntity<?> obtenerDetallePostulacion(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idPostulacion) {

        log.info("GET /api/coordinador/evaluacion/postulaciones/{}/detalle/{}", idUsuario, idPostulacion);

        try {
            DetallePostulacionCoordinadorDTO detalle = evaluacionService.obtenerDetallePostulacion(idUsuario, idPostulacion);
            return ResponseEntity.ok(detalle);
        } catch (RuntimeException e) {
            log.error("Error al obtener detalle: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    DictamenPostulacionResponseDTO.builder()
                            .exito(false)
                            .codigo("ERROR")
                            .mensaje(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Cambia el estado de una postulación a EN_REVISION cuando el coordinador la abre
     */
    @PostMapping("/postulaciones/{idUsuario}/iniciar-revision/{idPostulacion}")
    public ResponseEntity<CambioEstadoRevisionResponseDTO> iniciarRevision(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idPostulacion) {

        log.info("POST /api/coordinador/evaluacion/postulaciones/{}/iniciar-revision/{}", idUsuario, idPostulacion);

        CambioEstadoRevisionResponseDTO resultado = evaluacionService.cambiarEstadoARevision(idUsuario, idPostulacion);

        if (resultado.getExito()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }

    /**
     * Evalúa un documento individual (VALIDAR, OBSERVAR, RECHAZAR)
     */
    @PostMapping("/documentos/{idUsuario}/evaluar")
    public ResponseEntity<EvaluacionDocumentoResponseDTO> evaluarDocumento(
            @PathVariable Integer idUsuario,
            @Valid @RequestBody EvaluarDocumentoRequestDTO request,
            HttpServletRequest httpRequest) {

        log.info("POST /api/coordinador/evaluacion/documentos/{}/evaluar - Documento: {}, Acción: {}",
                idUsuario, request.getIdRequisitoAdjunto(), request.getAccion());

        EvaluacionDocumentoResponseDTO resultado = evaluacionService.evaluarDocumento(idUsuario, request);

        if (resultado.getExito()) {
            registrarLog(idUsuario, "EVALUAR_DOCUMENTO_" + request.getAccion().toUpperCase(),
                    "requisito_adjunto", request.getIdRequisitoAdjunto(),
                    null, "Acción: " + request.getAccion(), httpRequest);
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }

    /**
     * Dictamina (aprueba o rechaza) una postulación completa
     */
    @PostMapping("/postulaciones/{idUsuario}/dictaminar")
    public ResponseEntity<DictamenPostulacionResponseDTO> dictaminarPostulacion(
            @PathVariable Integer idUsuario,
            @Valid @RequestBody DictaminarPostulacionRequestDTO request,
            HttpServletRequest httpRequest) {

        log.info("POST /api/coordinador/evaluacion/postulaciones/{}/dictaminar - Postulación: {}, Acción: {}",
                idUsuario, request.getIdPostulacion(), request.getAccion());

        DictamenPostulacionResponseDTO resultado = evaluacionService.dictaminarPostulacion(idUsuario, request);

        if (resultado.getExito()) {
            registrarLog(idUsuario, "DICTAMINAR_POSTULACION_" + request.getAccion().toUpperCase(),
                    "postulacion", request.getIdPostulacion(),
                    "Postulación ID: " + request.getIdPostulacion(),
                    "Dictamen: " + request.getAccion() + (request.getObservacion() != null ? " | " + request.getObservacion() : ""),
                    httpRequest);
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }

    /**
     * Descarga el archivo de un documento adjunto
     */
    @GetMapping("/documentos/{idUsuario}/descargar/{idRequisitoAdjunto}")
    public ResponseEntity<byte[]> descargarDocumento(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRequisitoAdjunto) {

        log.info("GET /api/coordinador/evaluacion/documentos/{}/descargar/{}", idUsuario, idRequisitoAdjunto);

        try {
            byte[] archivo = evaluacionService.obtenerArchivoDocumento(idUsuario, idRequisitoAdjunto);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "documento.pdf");

            return new ResponseEntity<>(archivo, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error al descargar documento: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Visualiza el archivo de un documento adjunto (inline)
     */
    @GetMapping("/documentos/{idUsuario}/visualizar/{idRequisitoAdjunto}")
    public ResponseEntity<byte[]> visualizarDocumento(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRequisitoAdjunto) {

        log.info("GET /api/coordinador/evaluacion/documentos/{}/visualizar/{}", idUsuario, idRequisitoAdjunto);

        try {
            byte[] archivo = evaluacionService.obtenerArchivoDocumento(idUsuario, idRequisitoAdjunto);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=documento.pdf");

            return new ResponseEntity<>(archivo, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error al visualizar documento: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ---- Audit log helper ----
    private void registrarLog(Integer idUsuario, String accion, String tabla, Integer idRegistro,
                               String valorAnterior, String valorNuevo, HttpServletRequest request) {
        try {
            logAuditoriaService.registrar(LogAuditoriaRequestDTO.builder()
                    .idUsuario(idUsuario)
                    .accion(accion)
                    .tablaAfectada(tabla)
                    .registroAfectado(idRegistro)
                    .ipOrigen(request.getRemoteAddr())
                    .valorAnterior(valorAnterior)
                    .valorNuevo(valorNuevo)
                    .build());
        } catch (Exception ignored) {
            // El log no debe bloquear la operación principal
        }
    }
}
