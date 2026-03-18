package org.uteq.sgacfinal.controller.ayudantia;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.service.ayudantia.IAsistenciaService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AsistenciaController {
    private final IAsistenciaService asistenciaService;

    @GetMapping("/contexto")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<Map<String, Object>> obtenerContexto() {
        Map<String, Object> response = new HashMap<>();
        response.put("idAyudantia", asistenciaService.resolverIdAyudantia());
        response.put("idRegistro",  asistenciaService.resolverIdRegistro());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/participantes")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<Object> consultarParticipantes() {
        Integer idAyudantia = asistenciaService.resolverIdAyudantia();
        return ResponseEntity.ok(asistenciaService.consultarParticipantes(idAyudantia));
    }

    @PostMapping("/participantes/masivo")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Object> cargarParticipantesMasivo(
            @RequestBody Map<String, List<Map<String, String>>> body) {

        Integer idAyudantia   = asistenciaService.resolverIdAyudantia();
        List<Map<String, String>> participantes = body.get("participantes");
        return ResponseEntity.ok(
                asistenciaService.cargarParticipantesMasivo(idAyudantia, participantes));
    }

    @GetMapping("/plantilla-excel")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
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
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Object> previewExcel(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(asistenciaService.previewExcelImport(file));
    }

    @PostMapping("/inicializar")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<Object> inicializarAsistencia() {
        Integer idRegistro = asistenciaService.resolverIdRegistro();
        return ResponseEntity.ok(asistenciaService.inicializarAsistencia(idRegistro));
    }

    @PutMapping("/guardar")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Object> guardarAsistencias(
            @RequestBody Map<String, List<Map<String, Object>>> body) {

        Integer idRegistro = asistenciaService.resolverIdRegistro();
        return ResponseEntity.ok(
                asistenciaService.guardarAsistencias(idRegistro, body.get("asistencias")));
    }

    @GetMapping("/detalle")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<Object> consultarAsistencia() {
        Integer idRegistro = asistenciaService.resolverIdRegistro();
        return ResponseEntity.ok(asistenciaService.consultarAsistencia(idRegistro));
    }

    @GetMapping("/matriz")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<Object> obtenerMatriz() {
        return ResponseEntity.ok(asistenciaService.obtenerMatrizAsistencia());
    }
}