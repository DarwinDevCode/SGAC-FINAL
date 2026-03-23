package org.uteq.sgacfinal.service.impl;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import org.uteq.sgacfinal.entity.Ayudantia;
import org.uteq.sgacfinal.entity.RegistroActividad;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.repository.DocenteMisAyudantesRepository;
import org.uteq.sgacfinal.repository.EvidenciaRegistroActividadRepository;
import org.uteq.sgacfinal.repository.RegistroActividadConfigRepository;
import org.uteq.sgacfinal.service.IDocenteMisAyudantesService;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.IUsuarioSesionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DocenteMisAyudantesServiceImpl implements IDocenteMisAyudantesService {

    private final IUsuarioSesionService usuarioSesionService;
    private final DocenteMisAyudantesRepository docenteMisAyudantesRepository;
    private final RegistroActividadConfigRepository registroActividadConfigRepository;
    private final AyudantiaRepository ayudantiaRepository;
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
                    .horasCumplidas(r[12] != null ? ((java.math.BigDecimal) r[12]).intValue() : 0)
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
                        .codigoEstado((String) e[8])
                        .observaciones((String) e[9])
                        .fechaObservacion((LocalDate) e[10])
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
                    .codigoEstado((String) a[9])
                    .observaciones((String) a[10])
                    .fechaObservacion((LocalDate) a[11])
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

        String codigoEstado = null;
        if (request.getIdTipoEstadoRegistro() != null) {
            switch (request.getIdTipoEstadoRegistro()) {
                case 1 -> codigoEstado = "PENDIENTE";
                case 2 -> codigoEstado = "ACEPTADO";
                case 3 -> codigoEstado = "OBSERVADO";
                case 4 -> codigoEstado = "RECHAZADO";
                default -> codigoEstado = "PENDIENTE";
            }
        }
        
        Integer idRealEstado = docenteMisAyudantesRepository.getIdEstadoRegistroPorCodigo(codigoEstado);
        
        if (idRealEstado == null) {
            // Reintento con APROBADO por si la migración se cambió
            codigoEstado = "APROBADO";
            idRealEstado = docenteMisAyudantesRepository.getIdEstadoRegistroPorCodigo(codigoEstado);
        }

        if (idRealEstado == null) {
            throw new RuntimeException("Código de estado no válido en la base de datos para: " + codigoEstado);
        }

        // CARGAR ACTIVIDAD PARA ACTUALIZAR HORAS SI ES NECESARIO
        RegistroActividad ra = registroActividadConfigRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        String estadoAnterior = ra.getIdTipoEstadoRegistro() != null ? ra.getIdTipoEstadoRegistro().getCodigo() : "";
        
        LocalDate ahora = LocalDate.now();
        int updated = registroActividadConfigRepository.evaluarActividad(
                idActividad,
                idRealEstado,
                request.getObservaciones(),
                ahora
        );

        if (updated == 0) {
            throw new RuntimeException("No se pudo evaluar la actividad");
        }

        // --- ACTUALIZAR HORAS EN LA AYUDANTÍA ---
        boolean esAprobacion = "ACEPTADO".equals(codigoEstado) || "APROBADO".equals(codigoEstado);
        boolean eraAprobado = "ACEPTADO".equals(estadoAnterior) || "APROBADO".equals(estadoAnterior);

        if (esAprobacion && !eraAprobado) {
            // Sumar horas
            actualizarHorasAyudantia(ra.getAyudantia(), ra.getHorasDedicadas());
        } else if (!esAprobacion && eraAprobado) {
            // Restar horas (si se cambia de aprobado a otro estado)
            actualizarHorasAyudantia(ra.getAyudantia(), ra.getHorasDedicadas() != null ? ra.getHorasDedicadas().negate() : java.math.BigDecimal.ZERO);
        }

        // --- NOTIFICACIÓN AL AYUDANTE ---
        Integer idUsuarioAyudante = docenteMisAyudantesRepository.obtenerIdUsuarioAyudantePorActividad(idUsuario, idActividad);
        if (idUsuarioAyudante != null) {
            String titulo = "";
            String mensaje = "";
            String tipo = "INFO";

            if (esAprobacion) {
                titulo = "Actividad Aprobada";
                mensaje = "Tu actividad '" + ra.getTemaTratado() + "' ha sido aprobada. Se han sumado " + ra.getHorasDedicadas() + " horas.";
                tipo = "SUCCESS";
            } else if ("OBSERVADO".equals(codigoEstado)) {
                titulo = "Actividad Observada";
                mensaje = "Tienes una actividad observada: '" + ra.getTemaTratado() + "'. Tienes 24h para corregirla.";
                tipo = "WARNING";
            } else if ("RECHAZADO".equals(codigoEstado)) {
                titulo = "Actividad Rechazada";
                mensaje = "Tu actividad '" + ra.getTemaTratado() + "' ha sido rechazada.";
                tipo = "ERROR";
            }

            if (!titulo.isEmpty()) {
                notificacionService.enviarNotificacion(idUsuarioAyudante, NotificationRequest.builder()
                        .titulo(titulo)
                        .mensaje(mensaje)
                        .tipo(tipo)
                        .idReferencia(idActividad)
                        .build());
            }
        }
    }

    private void actualizarHorasAyudantia(Ayudantia ayudantia, java.math.BigDecimal delta) {
        if (delta == null) return;
        
        java.math.BigDecimal actuales = ayudantia.getHorasCumplidas() != null 
                ? ayudantia.getHorasCumplidas() 
                : java.math.BigDecimal.ZERO;
        
        ayudantia.setHorasCumplidas(actuales.add(delta));
        ayudantiaRepository.save(ayudantia);
    }

    @Override
    public void evaluarEvidencia(Integer idEvidencia, EvaluarEvidenciaRequestDTO request) {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        if (!evidenciaRegistroActividadRepository.evidenciaPerteneceAlDocente(idEvidencia, idUsuario)) {
            throw new RuntimeException("No autorizado para evaluar esta evidencia");
        }

        String codigoEstado = null;
        if (request.getIdTipoEstadoEvidencia() != null) {
            switch (request.getIdTipoEstadoEvidencia()) {
                case 1 -> codigoEstado = "SUBIDO";
                case 2 -> codigoEstado = "REVISADO";
                case 3 -> codigoEstado = "APROBADO";
                case 4 -> codigoEstado = "RECHAZADO";
                case 5 -> codigoEstado = "OBSERVADO";
                default -> codigoEstado = "SUBIDO";
            }
        }
        Integer idRealEstado = docenteMisAyudantesRepository.getIdEstadoEvidenciaPorCodigo(codigoEstado);
        
        if (idRealEstado == null) {
             throw new RuntimeException("Código de estado de evidencia no válido: " + codigoEstado);
        }

        LocalDate ahora = LocalDate.now();
        int updated = evidenciaRegistroActividadRepository.evaluarEvidencia(
                idEvidencia,
                idRealEstado,
                request.getObservaciones(),
                ahora
        );

        if (updated == 0) {
            throw new RuntimeException("No se pudo evaluar la evidencia");
        }

        // Notificación solo cuando cambia a OBSERVADO
        if ("OBSERVADO".equals(codigoEstado)) {
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

