package org.uteq.sgacfinal.controller.ayudantia;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.service.ayudantia.IAsistenciaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AsistenciaController {
    private final IAsistenciaService asistenciaService;

    @GetMapping("/ayudantia/{idAyudantia}/participantes")
    @PreAuthorize("hasAnyRole('AYUDANTE_CATEDRA', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<JsonNode> consultarParticipantes(
            @PathVariable Integer idAyudantia) {

        return ResponseEntity.ok(asistenciaService.consultarParticipantes(idAyudantia));
    }

    @PostMapping("/ayudantia/{idAyudantia}/participantes/masivo")
    @PreAuthorize("hasAnyRole('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<JsonNode> cargarParticipantesMasivo(
            @PathVariable Integer idAyudantia,
            @RequestBody Map<String, List<Map<String, String>>> body) {

        List<Map<String, String>> participantes = body.get("participantes");
        return ResponseEntity.ok(
                asistenciaService.cargarParticipantesMasivo(idAyudantia, participantes));
    }

    @GetMapping("/plantilla-excel")
    @PreAuthorize("hasAnyRole('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<byte[]> descargarPlantilla() {
        byte[] bytes = asistenciaService.generarPlantillaExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"plantilla_participantes.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping(value = "/preview-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<JsonNode> previewExcel(
            @RequestPart("file") MultipartFile file) {

        return ResponseEntity.ok(asistenciaService.previewExcelImport(file));
    }

    @PostMapping("/registro/{idRegistro}/inicializar")
    @PreAuthorize("hasAnyRole('AYUDANTE_CATEDRA')")
    public ResponseEntity<JsonNode> inicializarAsistencia(
            @PathVariable Integer idRegistro) {
        return ResponseEntity.ok(asistenciaService.inicializarAsistencia(idRegistro));
    }

    @PutMapping("/registro/{idRegistro}/guardar")
    @PreAuthorize("hasAnyRole('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<JsonNode> guardarAsistencias(
            @PathVariable Integer idRegistro,
            @RequestBody Map<String, List<Map<String, Object>>> body) {

        List<Map<String, Object>> asistencias = body.get("asistencias");
        return ResponseEntity.ok(asistenciaService.guardarAsistencias(idRegistro, asistencias));
    }

    @GetMapping("/registro/{idRegistro}")
    @PreAuthorize("hasAnyRole('AYUDANTE_CATEDRA', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<JsonNode> consultarAsistencia(
            @PathVariable Integer idRegistro) {

        return ResponseEntity.ok(asistenciaService.consultarAsistencia(idRegistro));
    }
}