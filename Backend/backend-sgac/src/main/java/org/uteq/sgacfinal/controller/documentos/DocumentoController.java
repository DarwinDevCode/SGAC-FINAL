package org.uteq.sgacfinal.controller.documentos;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.documentos.DocumentoUpdateRequestDTO;
import org.uteq.sgacfinal.dto.Response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.Response.documentos.*;
import org.uteq.sgacfinal.service.documentos.DocumentoService;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {
    private final DocumentoService documentoService;

    @GetMapping("/facultades")
    public ResponseEntity<RespuestaOperacionDTO<List<FacultadResponseDTO>>> getFacultades() {
        return ResponseEntity.ok(documentoService.getFacultades());
    }

    @GetMapping("/carreras/{idFacultad}")
    public ResponseEntity<RespuestaOperacionDTO<List<CarreraResponseDTO>>> getCarreras(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(documentoService.getCarreras(idFacultad));
    }

    @GetMapping("/tipos")
    public ResponseEntity<RespuestaOperacionDTO<List<TipoDocumentoResponseDTO>>> getTipos() {
        return ResponseEntity.ok(documentoService.getTiposDocumento());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<RespuestaOperacionDTO<DocumentoIdResponseDTO>> guardar(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("nombre") String nombre,
            @RequestParam("idTipo") Integer idTipo,
            @RequestParam("idUsuario") Integer idUsuario,
            @RequestParam(value = "idFacultad", required = false) Integer idFacultad,
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera
    ) {
        return ResponseEntity.ok(documentoService.guardarDocumento(archivo, nombre, idTipo, idUsuario, idFacultad, idCarrera));
    }

    @PutMapping
    public ResponseEntity<RespuestaOperacionDTO<Void>> actualizar(@RequestBody DocumentoUpdateRequestDTO req) {
        return ResponseEntity.ok(documentoService.actualizarDocumento(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaOperacionDTO<Void>> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(documentoService.eliminarDocumento(id));
    }

    @GetMapping("/visor")
    public ResponseEntity<RespuestaOperacionDTO<List<DocumentoVisorResponseDTO>>> listarVisor(
            @RequestParam Integer idUsuario,
            @RequestParam String rol
    ) {
        return ResponseEntity.ok(documentoService.listarVisor(idUsuario, rol));
    }
}