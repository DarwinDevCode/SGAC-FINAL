package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.entity.RequisitoAdjunto;
import org.uteq.sgacfinal.entity.TipoEstadoRequisito;
import org.uteq.sgacfinal.repository.RequisitoAdjuntoRepository;
import org.uteq.sgacfinal.service.IRequisitoAdjuntoService;

import java.util.List;

@RestController
@RequestMapping("/api/requisitos-adjuntos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RequisitoAdjuntoController {

    private final IRequisitoAdjuntoService requisitoService;
    private final RequisitoAdjuntoRepository requisitoRepository;

    /**
     * Lista los requisitos (sin el archivo binario) de una postulación.
     * El coordinador usa esto para ver qué documentos subió el estudiante.
     */
    @GetMapping("/postulacion/{idPostulacion}")
    public ResponseEntity<?> listarPorPostulacion(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(requisitoService.listarPorPostulacion(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar requisitos: " + e.getMessage());
        }
    }

    /**
     * Descarga el archivo de un requisito adjunto específico.
     * El coordinador puede hacer clic para ver/descargar el documento.
     */
    @GetMapping("/descargar/{idRequisito}")
    public ResponseEntity<?> descargar(@PathVariable Integer idRequisito) {
        RequisitoAdjunto requisito = requisitoRepository.findById(idRequisito)
                .orElseThrow(() -> new RuntimeException("Requisito no encontrado"));

        if (requisito.getArchivo() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Archivo no encontrado.");
        }

        String nombreArchivo = requisito.getNombreArchivo() != null ? requisito.getNombreArchivo() : "documento";
        MediaType mediaType = determinarMediaType(nombreArchivo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                .contentType(mediaType)
                .body(requisito.getArchivo());
    }

    private MediaType determinarMediaType(String nombreArchivo) {
        String lower = nombreArchivo.toLowerCase();
        if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * Ítem 8: El postulante reemplaza un documento observado.
     * PUT /api/requisitos-adjuntos/reemplazar/{idAdjunto}
     */
    @PutMapping("/reemplazar/{idAdjunto}")
    public ResponseEntity<?> reemplazar(@PathVariable Integer idAdjunto,
                                         @RequestParam("archivo") MultipartFile archivo) {
        try {
            return ResponseEntity.ok(requisitoService.reemplazar(idAdjunto, archivo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al reemplazar el documento: " + e.getMessage());
        }
    }

    /**
     * Observar un documento (Cambia estado parcial y agrega observación)
     * PUT /api/requisitos-adjuntos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> observarDocumento(
            @PathVariable Integer id,
            @RequestParam Integer idTipoEstadoRequisito,
            @RequestParam String observacion) {
        
        RequisitoAdjunto requisito = requisitoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisito no encontrado"));
                
        TipoEstadoRequisito estado = new TipoEstadoRequisito();
        estado.setIdTipoEstadoRequisito(idTipoEstadoRequisito);
        
        requisito.setTipoEstadoRequisito(estado);
        requisito.setObservacion(observacion);
        
        requisitoRepository.save(requisito);
        
        // Return JSON explicitly as the frontend expects { "nombreEstado": "..." }
        return ResponseEntity.ok("{\"nombreEstado\": \"OBSERVADO\", \"message\": \"Observación guardada.\"}");
    }

    /**
     * Subsana un documento observado con validaciones de fecha y notificación al coordinador.
     * PUT /api/requisitos-adjuntos/subsanar/{idUsuario}/{idAdjunto}
     *
     * Validaciones:
     * 1. El documento debe estar en estado OBSERVADO
     * 2. Debe estar dentro del periodo de subsanación (etapa de revisión)
     *
     * Acciones:
     * 1. Actualiza el archivo
     * 2. Cambia estado a CORREGIDO
     * 3. Notifica automáticamente al coordinador
     */
    @PutMapping("/subsanar/{idUsuario}/{idAdjunto}")
    public ResponseEntity<?> subsanarDocumento(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idAdjunto,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            return ResponseEntity.ok(requisitoService.subsanarDocumentoObservado(idUsuario, idAdjunto, archivo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"exito\": false, \"mensaje\": \"Error al subsanar: " + e.getMessage() + "\"}");
        }
    }
}
