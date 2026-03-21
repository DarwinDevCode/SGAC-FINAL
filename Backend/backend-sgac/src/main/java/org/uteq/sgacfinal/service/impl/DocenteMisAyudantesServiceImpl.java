package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.EvaluarActividadRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.dto.response.ActividadDetalleDTO;
import org.uteq.sgacfinal.dto.response.AyudanteResponseDTO;
import org.uteq.sgacfinal.repository.DocenteMisAyudantesRepository;
import org.uteq.sgacfinal.repository.EvidenciaRegistroActividadRepository;
import org.uteq.sgacfinal.repository.RegistroActividadConfigRepository;
import org.uteq.sgacfinal.service.IDocenteMisAyudantesService;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.IUsuarioSesionService;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocenteMisAyudantesServiceImpl implements IDocenteMisAyudantesService {

    private final IUsuarioSesionService usuarioSesionService;
    private final DocenteMisAyudantesRepository docenteMisAyudantesRepository;
    private final RegistroActividadConfigRepository registroActividadConfigRepository;
    private final EvidenciaRegistroActividadRepository evidenciaRegistroActividadRepository;
    private final INotificacionService notificacionService;

    @Override
    @Transactional(readOnly = true)
    public List<AyudanteResponseDTO> listarMisAyudantes() {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();
        List<Object[]> rows = docenteMisAyudantesRepository.listarMisAyudantes(idUsuario);
        List<AyudanteResponseDTO> out = new ArrayList<>();

        for (Object[] r : rows) {
            // (ver SELECT en repository)
            out.add(AyudanteResponseDTO.builder()
                    .idAyudantia((Integer) r[0])
                    .idConvocatoria((Integer) r[1])
                    .idPeriodoAcademico((Integer) r[2])
                    .periodoAcademico((String) r[3])
                    .idAsignatura((Integer) r[4])
                    .asignatura((String) r[5])
                    .idUsuarioAyudante((Integer) r[6])
                    .nombresAyudante((String) r[7])
                    .apellidosAyudante((String) r[8])
                    .fechaInicio((LocalDate) r[9])
                    .fechaFin((LocalDate) r[10])
                    .horasMaximas((java.math.BigDecimal) r[11])
                    .horasCumplidas((Integer) r[12])
                    .estadoAyudantia((String) r[13])
                    .build());
        }

        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadDetalleDTO> listarActividadesPorAyudantia(Integer idAyudantia) {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();
        List<Object[]> actividades = docenteMisAyudantesRepository.listarActividadesAyudantia(idUsuario, idAyudantia);

        List<ActividadDetalleDTO> out = new ArrayList<>();
        for (Object[] a : actividades) {
            Integer idRegistro = (Integer) a[0];
            List<Object[]> evidencias = docenteMisAyudantesRepository.listarEvidenciasRegistro(idUsuario, idRegistro);

            List<ActividadDetalleDTO.EvidenciaDetalleDocenteDTO> evidenciasDto = new ArrayList<>();
            for (Object[] e : evidencias) {
                evidenciasDto.add(ActividadDetalleDTO.EvidenciaDetalleDocenteDTO.builder()
                        .idEvidencia((Integer) e[0])
                        .nombreArchivo((String) e[1])
                        .rutaArchivo((String) e[2])
                        .mimeType((String) e[3])
                        .tamanioBytes((Integer) e[4])
                        .fechaSubida((LocalDate) e[5])
                        .idTipoEstadoEvidencia((Integer) e[6])
                        .estadoEvidencia((String) e[7])
                        .observaciones((String) e[8])
                        .fechaObservacion((LocalDate) e[9])
                        .build());
            }

            out.add(ActividadDetalleDTO.builder()
                    .idRegistroActividad(idRegistro)
                    .idAyudantia((Integer) a[1])
                    .descripcionActividad((String) a[2])
                    .temaTratado((String) a[3])
                    .fecha((LocalDate) a[4])
                    .numeroAsistentes((Integer) a[5])
                    .horasDedicadas((java.math.BigDecimal) a[6])
                    .idTipoEstadoRegistro((Integer) a[7])
                    .estadoRegistro((String) a[8])
                    .observaciones((String) a[9])
                    .fechaObservacion((LocalDate) a[10])
                    .evidencias(evidenciasDto)
                    .build());
        }

        return out;
    }

    @Override
    public void evaluarActividad(Integer idActividad, EvaluarActividadRequestDTO request) {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        if (!registroActividadConfigRepository.perteneceAlDocente(idActividad, idUsuario)) {
            throw new RuntimeException("No autorizado para evaluar esta actividad");
        }

        // Siempre actualiza fecha_observacion al momento de evaluar (requisito)
        LocalDate ahora = LocalDate.now();
        int updated = registroActividadConfigRepository.evaluarActividad(
                idActividad,
                request.getIdTipoEstadoRegistro(),
                request.getObservaciones(),
                ahora
        );

        if (updated == 0) {
            throw new RuntimeException("No se pudo evaluar la actividad");
        }

        // Notificación solo cuando cambia a OBSERVADO (3)
        if (request.getIdTipoEstadoRegistro() != null && request.getIdTipoEstadoRegistro() == 3) {
            Integer idUsuarioAyudante = docenteMisAyudantesRepository
                    .obtenerIdUsuarioAyudantePorActividad(idUsuario, idActividad);

            if (idUsuarioAyudante != null) {
                notificacionService.enviarNotificacion(idUsuarioAyudante, NotificationRequest.builder()
                        .titulo("Actividad observada")
                        .mensaje("Tienes una actividad observada. Tu plazo de 24 horas para corregir ha iniciado.")
                        .tipo("OBSERVACION")
                        .idReferencia(idActividad)
                        .build());
            }
        }
    }

    @Override
    public void evaluarEvidencia(Integer idEvidencia, EvaluarEvidenciaRequestDTO request) {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        if (!evidenciaRegistroActividadRepository.evidenciaPerteneceAlDocente(idEvidencia, idUsuario)) {
            throw new RuntimeException("No autorizado para evaluar esta evidencia");
        }

        LocalDate ahora = LocalDate.now();
        int updated = evidenciaRegistroActividadRepository.evaluarEvidencia(
                idEvidencia,
                request.getIdTipoEstadoEvidencia(),
                request.getObservaciones(),
                ahora
        );

        if (updated == 0) {
            throw new RuntimeException("No se pudo evaluar la evidencia");
        }

        // Notificación solo cuando cambia a OBSERVADO (5)
        if (request.getIdTipoEstadoEvidencia() != null && request.getIdTipoEstadoEvidencia() == 5) {
            Integer idUsuarioAyudante = docenteMisAyudantesRepository
                    .obtenerIdUsuarioAyudantePorEvidencia(idUsuario, idEvidencia);

            if (idUsuarioAyudante != null) {
                notificacionService.enviarNotificacion(idUsuarioAyudante, NotificationRequest.builder()
                        .titulo("Evidencia observada")
                        .mensaje("Una evidencia fue observada. Tu plazo de 24 horas para corregir ha iniciado.")
                        .tipo("OBSERVACION")
                        .idReferencia(idEvidencia)
                        .build());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> descargarEvidencia(Integer idEvidencia) {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        // Seguridad: solo evidencias de ayudantías del docente
        String ruta = docenteMisAyudantesRepository.obtenerRutaArchivoEvidencia(idUsuario, idEvidencia);
        if (ruta == null || ruta.isBlank()) {
            throw new RuntimeException("Evidencia no encontrada o no autorizada");
        }

        // Si es URL (cloudinary u otra), devolvemos un redirect.
        if (ruta.startsWith("http://") || ruta.startsWith("https://")) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, ruta)
                    .build();
        }

        File file = new File(ruta);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("Archivo no encontrado en la ruta especificada");
        }

        Resource resource = new FileSystemResource(file);

        String filename = file.getName();
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentLength(file.length())
                .body(resource);
    }
}

