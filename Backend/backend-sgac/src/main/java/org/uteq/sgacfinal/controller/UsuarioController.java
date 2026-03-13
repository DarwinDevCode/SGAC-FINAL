package org.uteq.sgacfinal.controller;

import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.UsuarioRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.service.IPdfGeneratorService;
import org.uteq.sgacfinal.service.IExcelGeneratorService;
import org.uteq.sgacfinal.service.IUsuariosService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/api/usuarios", "/api/admin/usuarios"})
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final IUsuariosService usuarioService;
    private final IPdfGeneratorService pdfGeneratorService;
    private final IExcelGeneratorService excelGeneratorService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/reporte")
    public ResponseEntity<byte[]> descargarReporteUsuarios() {
        try {
            List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
            byte[] pdfBytes = pdfGeneratorService.generarReporteUsuarios(usuarios);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_usuarios.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar PDF de usuarios: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> descargarReporteUsuariosExcel() {
        try {
            List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
            byte[] excelBytes = excelGeneratorService.generarExcelUsuarios(usuarios);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_usuarios.xlsx");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
