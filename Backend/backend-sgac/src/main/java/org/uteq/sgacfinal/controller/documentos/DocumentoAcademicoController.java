package org.uteq.sgacfinal.controller.documentos;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.entity.DocumentoAcademico;
import org.uteq.sgacfinal.service.documentos.IDocumentoAcademicoService;

import java.util.List;

@RestController
@RequestMapping("/api/documentos-academicos")
@RequiredArgsConstructor
public class DocumentoAcademicoController {

    private final IDocumentoAcademicoService service;

    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<DocumentoAcademico> subir(
            @RequestPart("file") MultipartFile file,
            @RequestParam String nombre,
            @RequestParam Integer idTipo,
            @RequestParam Integer idPeriodo,
            @RequestParam(required = false) Integer idConvocatoria) {

        return ResponseEntity.ok(service.subirDocumento(file, nombre, idTipo, idPeriodo, idConvocatoria));
    }



    @GetMapping("/visor")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentoAcademico>> listarParaUsuario(
            @RequestParam Integer idPeriodo,
            @RequestParam(required = false) Integer idConvocatoria) {
        return ResponseEntity.ok(service.listarParaActores(idPeriodo, idConvocatoria));
    }
}