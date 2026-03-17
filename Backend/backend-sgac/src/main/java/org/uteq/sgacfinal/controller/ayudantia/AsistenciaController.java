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

    /**
     * Obtiene el contexto inicial (IDs) del ayudante autenticado.
     * Cambiado de ObjectNode a Map para evitar metadatos técnicos.
     */
    @GetMapping("/contexto")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<Map<String, Object>> obtenerContexto() {
        Integer idAyudantia = asistenciaService.resolverIdAyudantia();
        Integer idRegistro  = asistenciaService.resolverIdRegistro();

        Map<String, Object> response = new HashMap<>();
        response.put("idAyudantia", idAyudantia);
        response.put("idRegistro",  idRegistro);

        return ResponseEntity.ok(response);
    }

    /**
     * Consulta los participantes. Retorna Object (Map/List) serializado limpiamente.
     */
    @GetMapping("/participantes")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<Object> consultarParticipantes() {
        Integer idAyudantia = asistenciaService.resolverIdAyudantia();
        return ResponseEntity.ok(asistenciaService.consultarParticipantes(idAyudantia));
    }

    /**
     * Carga masiva. Recibe el body y delega al service que ahora devuelve Object.
     */
    @PostMapping("/participantes/masivo")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Object> cargarParticipantesMasivo(
            @RequestBody Map<String, List<Map<String, String>>> body) {

        Integer idAyudantia = asistenciaService.resolverIdAyudantia();
        List<Map<String, String>> participantes = body.get("participantes");

        return ResponseEntity.ok(asistenciaService.cargarParticipantesMasivo(idAyudantia, participantes));
    }

    /**
     * Descarga la plantilla Excel (Se mantiene igual, devuelve byte[]).
     */
    @GetMapping("/plantilla-excel")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<byte[]> descargarPlantilla() {
        byte[] bytes = asistenciaService.generarPlantillaExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla_participantes.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    /**
     * Previsualización del Excel. Retorna el mapa de filas y errores.
     */
    @PostMapping(value = "/preview-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Object> previewExcel(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(asistenciaService.previewExcelImport(file));
    }

    /**
     * Inicializa la asistencia para el registro actual.
     */
    @PostMapping("/inicializar")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA')")
    public ResponseEntity<Object> inicializarAsistencia() {
        Integer idRegistro = asistenciaService.resolverIdRegistro();
        return ResponseEntity.ok(asistenciaService.inicializarAsistencia(idRegistro));
    }

    /**
     * Guarda el estado de asistencia.
     */
    @PutMapping("/guardar")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Object> guardarAsistencias(
            @RequestBody Map<String, List<Map<String, Object>>> body) {

        Integer idRegistro = asistenciaService.resolverIdRegistro();
        List<Map<String, Object>> asistencias = body.get("asistencias");

        return ResponseEntity.ok(asistenciaService.guardarAsistencias(idRegistro, asistencias));
    }

    /**
     * Consulta el detalle de asistencia.
     */
    @GetMapping("/detalle")
    @PreAuthorize("hasAnyAuthority('AYUDANTE_CATEDRA', 'COORDINADOR', 'DECANO', 'ADMINISTRADOR')")
    public ResponseEntity<Object> consultarAsistencia() {
        Integer idRegistro = asistenciaService.resolverIdRegistro();
        return ResponseEntity.ok(asistenciaService.consultarAsistencia(idRegistro));
    }
}