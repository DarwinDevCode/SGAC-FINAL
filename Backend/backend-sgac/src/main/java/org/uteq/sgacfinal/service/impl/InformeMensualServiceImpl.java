package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.InformeMensualRequest;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.dto.response.InformeMensualResponse;
import org.uteq.sgacfinal.dto.response.SesionListadoResponse;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.exception.BadRequestException;
import org.uteq.sgacfinal.exception.RecursoNoEncontradoException;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.repository.InformeMensualRepository;
import org.uteq.sgacfinal.repository.TipoEstadoInformeRepository;
import org.uteq.sgacfinal.service.AiReportService;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.InformeMensualService;
import org.uteq.sgacfinal.service.SesionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InformeMensualServiceImpl implements InformeMensualService {

    private static final Logger log = LoggerFactory.getLogger(InformeMensualServiceImpl.class);

    private final InformeMensualRepository informeRepository;
    private final TipoEstadoInformeRepository estadoInformeRepository;
    private final AyudantiaRepository ayudantiaRepository;
    private final SesionService sesionService;
    private final AiReportService aiReportService;
    private final INotificacionService notificacionService;
    private final ObjectMapper objectMapper;

    public InformeMensualServiceImpl(
            InformeMensualRepository informeRepository,
            TipoEstadoInformeRepository estadoInformeRepository,
            AyudantiaRepository ayudantiaRepository,
            SesionService sesionService,
            AiReportService aiReportService,
            INotificacionService notificacionService,
            ObjectMapper objectMapper) {
        this.informeRepository = informeRepository;
        this.estadoInformeRepository = estadoInformeRepository;
        this.ayudantiaRepository = ayudantiaRepository;
        this.sesionService = sesionService;
        this.aiReportService = aiReportService;
        this.notificacionService = notificacionService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public InformeMensualResponse generarBorradorConIAUsuario(Integer idUsuario, Integer mes, Integer anio) {
        Integer idAyudantia = ayudantiaRepository.findIdAyudantiaActivaByUsuario(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No tiene ayudantía activa."));
        return generarBorradorInternal(idAyudantia, idUsuario, mes, anio);
    }

    private InformeMensualResponse generarBorradorInternal(Integer idAyudantia, Integer idUsuario, Integer mes, Integer anio) {
        Ayudantia ayudantia = ayudantiaRepository.findById(idAyudantia)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ayudantía no encontrada."));

        LocalDate fechaDesde = LocalDate.of(anio, mes, 1);
        LocalDate fechaHasta = fechaDesde.withDayOfMonth(fechaDesde.lengthOfMonth());

        List<SesionListadoResponse> sesiones = sesionService.listarSesiones(
                idUsuario,
                fechaDesde,
                fechaHasta,
                null,
                null);

        if (sesiones.isEmpty()) {
            throw new BadRequestException("No hay sesiones registradas en este mes para generar un informe.");
        }

        try {
            String payloadJson = objectMapper.writeValueAsString(sesiones.stream().map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("fecha", s.getFecha() != null ? s.getFecha().toString() : "");
                map.put("temaTratado", s.getTemaTratado());
                map.put("descripcion", s.getDescripcion());
                map.put("horasObtenidas", s.getHorasDedicadas());
                return map;
            }).collect(Collectors.toList()));

            String borradorGenerado = aiReportService.generateBorradorInforme(payloadJson);

            TipoEstadoInforme estadoBorrador = estadoInformeRepository.findByCodigo("BORRADOR_GENERADO")
                    .orElseThrow(() -> new RuntimeException("Estado BORRADOR_GENERADO no encontrado"));

            Optional<InformeMensual> informeOpt = informeRepository.findByAyudantia_IdAyudantiaAndMesAndAnio(idAyudantia, mes, anio);
            InformeMensual informe;
            if (informeOpt.isPresent()) {
                informe = informeOpt.get();
                if (!List.of("NO_INICIADO", "EN_ELABORACION", "BORRADOR_GENERADO", "OBSERVADO")
                        .contains(informe.getTipoEstadoInforme().getCodigo())) {
                    throw new BadRequestException("El informe ya se encuentra en proceso de revisión o aprobado.");
                }
            } else {
                informe = InformeMensual.builder()
                        .ayudantia(ayudantia)
                        .periodoAcademico(ayudantia.getPostulacion().getConvocatoria().getPeriodoAcademico())
                        .mes(mes)
                        .anio(anio)
                        .build();
            }

            informe.setContenidoBorrador(borradorGenerado);
            informe.setTipoEstadoInforme(estadoBorrador);
            informe.setFechaGeneracion(LocalDateTime.now());

            informeRepository.save(informe);

            return mapToDto(informe);
        } catch (BadRequestException | RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error crítico al generar borrador de informe: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar la información de las sesiones: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public InformeMensualResponse enviarARevision(Integer idInformeMensual, InformeMensualRequest request) {
        InformeMensual informe = getInformeOrThrow(idInformeMensual);

        if (!"BORRADOR_GENERADO".equals(informe.getTipoEstadoInforme().getCodigo()) &&
                !"EN_ELABORACION".equals(informe.getTipoEstadoInforme().getCodigo())) {
            throw new BadRequestException("El informe debe estar en estado de borrador para enviarlo.");
        }

        TipoEstadoInforme estadoRevision = estadoInformeRepository.findByCodigo("EN_REVISION_DOCENTE")
                .orElseThrow(() -> new RuntimeException("Estado EN_REVISION_DOCENTE no encontrado"));

        if (request.getBorradorEditado() != null && !request.getBorradorEditado().isEmpty()) {
            informe.setContenidoBorrador(request.getBorradorEditado());
        }

        informe.setTipoEstadoInforme(estadoRevision);
        informe.setFechaEnvio(LocalDateTime.now());
        informeRepository.save(informe);

        Docente docente = informe.getAyudantia().getPostulacion().getConvocatoria().getDocente();
        notificacionService.enviarNotificacion(
                docente.getUsuario().getIdUsuario(),
                NotificationRequest.builder()
                        .titulo("Nuevo informe pendiente de revisión")
                        .mensaje("El ayudante " + informe.getAyudantia().getPostulacion().getEstudiante().getUsuario().getNombres() + " " +
                                informe.getAyudantia().getPostulacion().getEstudiante().getUsuario().getApellidos() +
                                " ha enviado su informe mensual correspondiente al mes " + informe.getMes() + " - " + informe.getAnio() + ".")
                        .tipo("INFO")
                        .build()
        );

        return mapToDto(informe);
    }

    @Override
    @Transactional
    public InformeMensualResponse aprobarInforme(Integer idInformeMensual, String rolDeAprobacion) {
        InformeMensual informe = getInformeOrThrow(idInformeMensual);
        String estadoActual = informe.getTipoEstadoInforme().getCodigo();
        String nuevoEstadoCodigo;

        if ("DOCENTE".equalsIgnoreCase(rolDeAprobacion) && "EN_REVISION_DOCENTE".equals(estadoActual)) {
            nuevoEstadoCodigo = "EN_REVISION_COORDINADOR";
        } else if ("COORDINADOR".equalsIgnoreCase(rolDeAprobacion) && "EN_REVISION_COORDINADOR".equals(estadoActual)) {
            nuevoEstadoCodigo = "EN_REVISION_DECANO";
        } else if ("DECANO".equalsIgnoreCase(rolDeAprobacion) && "EN_REVISION_DECANO".equals(estadoActual)) {
            nuevoEstadoCodigo = "APROBADO";
        } else {
            throw new BadRequestException("Transición no permitida para el rol indicado o estado actual: " + estadoActual);
        }

        TipoEstadoInforme nuevoEstado = estadoInformeRepository.findByCodigo(nuevoEstadoCodigo)
                .orElseThrow(() -> new RuntimeException("Estado " + nuevoEstadoCodigo + " no encontrado"));

        informe.setTipoEstadoInforme(nuevoEstado);
        informe.setObservaciones(null);
        informeRepository.save(informe);

        notificacionService.enviarNotificacion(
                informe.getAyudantia().getPostulacion().getEstudiante().getUsuario().getIdUsuario(),
                NotificationRequest.builder()
                        .titulo("Informe Mensual Avanzado")
                        .mensaje("Su informe del mes " + informe.getMes() + " - " + informe.getAnio() + " ha avanzado al estado: " + nuevoEstado.getNombreEstado())
                        .tipo("SUCCESS")
                        .build()
        );

        return mapToDto(informe);
    }

    @Override
    @Transactional
    public InformeMensualResponse observarInforme(Integer idInformeMensual, String observaciones) {
        InformeMensual informe = getInformeOrThrow(idInformeMensual);

        TipoEstadoInforme nuevoEstado = estadoInformeRepository.findByCodigo("EN_ELABORACION")
                .orElseThrow(() -> new RuntimeException("Estado EN_ELABORACION no encontrado"));

        informe.setTipoEstadoInforme(nuevoEstado);
        informe.setObservaciones(observaciones);
        informeRepository.save(informe);

        notificacionService.enviarNotificacion(
                informe.getAyudantia().getPostulacion().getEstudiante().getUsuario().getIdUsuario(),
                NotificationRequest.builder()
                        .titulo("Informe Mensual Observado")
                        .mensaje("Su informe del mes " + informe.getMes() + " - " + informe.getAnio() + " ha sido observado. Por favor corrija: " + observaciones)
                        .tipo("WARNING")
                        .build()
        );

        return mapToDto(informe);
    }

    @Override
    @Transactional
    public InformeMensualResponse rechazarInforme(Integer idInformeMensual, String motivo) {
        InformeMensual informe = getInformeOrThrow(idInformeMensual);

        TipoEstadoInforme nuevoEstado = estadoInformeRepository.findByCodigo("REZAGADO")
                .orElseThrow(() -> new RuntimeException("Estado REZAGADO no encontrado"));

        informe.setTipoEstadoInforme(nuevoEstado);
        informe.setObservaciones(motivo);
        informeRepository.save(informe);

        notificacionService.enviarNotificacion(
                informe.getAyudantia().getPostulacion().getEstudiante().getUsuario().getIdUsuario(),
                NotificationRequest.builder()
                        .titulo("Informe Mensual Rechazado")
                        .mensaje("Su informe del mes " + informe.getMes() + " - " + informe.getAnio() + " ha sido rechazado permanentemente. Motivo: " + motivo)
                        .tipo("ERROR")
                        .build()
        );

        return mapToDto(informe);
    }

    @Override
    public List<InformeMensualResponse> listarMisInformes(Integer idUsuario) {
        Optional<Integer> idAyudantiaOpt = ayudantiaRepository.findIdAyudantiaActivaByUsuario(idUsuario);
        if (idAyudantiaOpt.isEmpty()) {
            return List.of();
        }
        return listarInformesPorAyudantia(idAyudantiaOpt.get());
    }

    @Override
    public List<InformeMensualResponse> listarInformesPorAyudantia(Integer idAyudantia) {
        return informeRepository.findByAyudantia_IdAyudantia(idAyudantia).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InformeMensualResponse obtenerDetalle(Integer idInformeMensual) {
        return mapToDto(getInformeOrThrow(idInformeMensual));
    }

    @Override
    public List<InformeMensualResponse> listarInformesPorDocenteYEstado(Integer idDocente, String codigoEstado) {
        return informeRepository.findAll().stream()
                .filter(inf -> inf.getAyudantia().getPostulacion().getConvocatoria().getDocente().getIdDocente().equals(idDocente))
                .filter(inf -> inf.getTipoEstadoInforme().getCodigo().equals(codigoEstado))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InformeMensualResponse> listarInformesPorEstado(String codigoEstado) {
        return informeRepository.findAll().stream()
                .filter(inf -> inf.getTipoEstadoInforme().getCodigo().equals(codigoEstado))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private InformeMensual getInformeOrThrow(Integer id) {
        return informeRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Informe mensual no encontrado."));
    }

    private InformeMensualResponse mapToDto(InformeMensual informe) {
        return InformeMensualResponse.builder()
                .idInformeMensual(informe.getIdInformeMensual())
                .idAyudantia(informe.getAyudantia().getIdAyudantia())
                .mes(informe.getMes())
                .anio(informe.getAnio())
                .contenidoBorrador(informe.getContenidoBorrador())
                .estado(informe.getTipoEstadoInforme().getCodigo())
                .fechaGeneracion(informe.getFechaGeneracion())
                .fechaEnvio(informe.getFechaEnvio())
                .observaciones(informe.getObservaciones())
                .build();
    }
}
