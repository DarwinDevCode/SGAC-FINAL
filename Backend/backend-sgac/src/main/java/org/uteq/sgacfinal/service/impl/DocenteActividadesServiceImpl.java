package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.CambiarEstadoActividadRequest;
import org.uteq.sgacfinal.dto.Request.CambiarEstadoEvidenciaRequest;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.IDocenteActividadesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocenteActividadesServiceImpl implements IDocenteActividadesService {

    private final RegistroActividadRepository registroRepo;
    private final EvidenciaRegistroActividadRepository evidenciaRepo;
    private final DocenteRepository docenteRepo;
    private final TipoEstadoRegistroRepository estadoRegistroRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public DocenteDashboardDTO getDashboard(Integer idUsuarioDocente) {
       return  new DocenteDashboardDTO();
    }

    @Override
    public List<AyudanteResumenDTO> listarAyudantes(Integer idUsuarioDocente) {
        List<Object[]> rows = registroRepo.spListarAyudantesDocente(idUsuarioDocente);
        return rows.stream().map(r -> AyudanteResumenDTO.builder()
                .idAyudantia(toInt(r[0]))
                .idUsuario(toInt(r[1]))
                .nombreCompleto(str(r[2]))
                .correo(str(r[3]))
                .nombreAsignatura(str(r[4]))
                .estadoAyudantia(str(r[5]))
                .horasCumplidas(toInt(r[6]))
                .actividadesTotal(toLong(r[7]))
                .actividadesPendientes(toLong(r[8]))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<RegistroActividadDocenteDTO> listarActividadesAyudante(Integer idAyudantia) {
        List<Object[]> rows = registroRepo.spActividadesAyudanteDocente(idAyudantia);
        return rows.stream().map(r -> {
            Integer idRegistro = toInt(r[0]);
            List<EvidenciaDocenteDTO> evidencias = obtenerEvidencias(idRegistro);
            return RegistroActividadDocenteDTO.builder()
                    .idRegistroActividad(idRegistro)
                    .idAyudantia(idAyudantia)
                    .descripcionActividad(str(r[1]))
                    .temaTratado(str(r[2]))
                    .fecha(toDate(r[3]))
                    .numeroAsistentes(toInt(r[4]))
                    .horasDedicadas(toBigDecimal(r[5]))
                    .estadoRevision(str(r[6]))
                    .observaciones(str(r[7]))
                    .fechaObservacion(toDate(r[8]))
                    .evidencias(evidencias)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public RegistroActividadDocenteDTO getDetalleActividad(Integer idRegistroActividad) {
        RegistroActividad ra = registroRepo.findById(idRegistroActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        List<EvidenciaDocenteDTO> evidencias = obtenerEvidencias(idRegistroActividad);
        return RegistroActividadDocenteDTO.builder()
                .idRegistroActividad(ra.getIdRegistroActividad())
                .idAyudantia(ra.getAyudantia().getIdAyudantia())
                .descripcionActividad(ra.getDescripcionActividad())
                .temaTratado(ra.getTemaTratado())
                .fecha(ra.getFecha())
                .numeroAsistentes(ra.getNumeroAsistentes())
                .horasDedicadas(ra.getHorasDedicadas())
                .estadoRevision(ra.getIdTipoEstadoRegistro() != null
                        ? ra.getIdTipoEstadoRegistro().getNombreEstado() : null)
                .observaciones(ra.getObservaciones())
                .fechaObservacion(ra.getFechaObservacion())
                .evidencias(evidencias)
                .build();
    }

    private List<EvidenciaDocenteDTO> obtenerEvidencias(Integer idRegistro) {
        List<Object[]> evRows = registroRepo.spEvidenciasActividadDocente(idRegistro);
        return evRows.stream().map(e -> EvidenciaDocenteDTO.builder()
                .idEvidencia(toInt(e[0]))
                .tipoEvidencia(str(e[1]))
                .nombreArchivo(str(e[2]))
                .rutaArchivo(str(e[3]))
                .mimeType(str(e[4]))
                .fechaSubida(toDate(e[5]))
                .estadoEvidencia(str(e[6]))
                .observaciones(str(e[7]))
                .fechaObservacion(toDate(e[8]))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarEstadoActividad(Integer idRegistroActividad,
                                       CambiarEstadoActividadRequest request,
                                       Integer idUsuarioDocente) {

        RegistroActividad ra = registroRepo.findById(idRegistroActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        TipoEstadoRegistro nuevoEstado = estadoRegistroRepo.findAll().stream()
                .filter(e -> e.getNombreEstado().equalsIgnoreCase(request.getEstado()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Estado inválido: " + request.getEstado()));

        ra.setIdTipoEstadoRegistro(nuevoEstado);
        ra.setObservaciones(request.getObservaciones());
        ra.setFechaObservacion(LocalDate.now());
        registroRepo.save(ra);

        if ("OBSERVADO".equalsIgnoreCase(request.getEstado()) && request.getObservaciones() != null) {
            notificarObservacionActividad(ra, request.getObservaciones(), idUsuarioDocente);
        }
    }

    @Override
    @Transactional
    public void cambiarEstadoEvidencia(Integer idEvidencia,
                                       CambiarEstadoEvidenciaRequest request,
                                       Integer idUsuarioDocente) {
        return;
    }

    private void notificarObservacionActividad(RegistroActividad ra, String obs, Integer idDocente) {
        try {
            Integer idUsuarioAyudante = ra.getAyudantia()
                    .getPostulacion().getEstudiante().getUsuario().getIdUsuario();

            Docente docente = docenteRepo.findByUsuario_IdUsuario(idDocente).orElse(null);
            String nombreDoc = docente != null
                    ? docente.getUsuario().getNombres() + " " + docente.getUsuario().getApellidos()
                    : "Docente";

            ObservacionWsDTO payload = ObservacionWsDTO.builder()
                    .tipo("ACTIVIDAD")
                    .idReferencia(ra.getIdRegistroActividad())
                    .nombreActividad(ra.getDescripcionActividad())
                    .observacion(obs)
                    .estadoNuevo("OBSERVADO")
                    .fecha(LocalDate.now())
                    .nombreDocente(nombreDoc)
                    .build();

            messagingTemplate.convertAndSend("/queue/observaciones/" + idUsuarioAyudante, payload);
        } catch (Exception ex) {
            // No bloquear si falla el WS
        }
    }

    private void notificarObservacionEvidencia(EvidenciaRegistroActividad ev, String obs, Integer idDocente) {
        try {
            Integer idUsuarioAyudante = ev.getRegistroActividad()
                    .getAyudantia().getPostulacion().getEstudiante().getUsuario().getIdUsuario();

            Docente docente = docenteRepo.findByUsuario_IdUsuario(idDocente).orElse(null);
            String nombreDoc = docente != null
                    ? docente.getUsuario().getNombres() + " " + docente.getUsuario().getApellidos()
                    : "Docente";

            ObservacionWsDTO payload = ObservacionWsDTO.builder()
                    .tipo("EVIDENCIA")
                    .idReferencia(ev.getIdEvidenciaRegistroActividad())
                    .nombreActividad("Evidencia: " + ev.getNombreArchivo())
                    .observacion(obs)
                    .estadoNuevo("OBSERVADO")
                    .fecha(LocalDate.now())
                    .nombreDocente(nombreDoc)
                    .build();

            messagingTemplate.convertAndSend("/queue/observaciones/" + idUsuarioAyudante, payload);
        } catch (Exception ex) {
            // No bloquear si falla el WS
        }
    }

    private long toLong(Object o) { return o == null ? 0L : ((Number) o).longValue(); }
    private int toInt(Object o) { return o == null ? 0 : ((Number) o).intValue(); }
    private String str(Object o) { return o == null ? null : o.toString(); }
    private BigDecimal toBigDecimal(Object o) { return o == null ? null : new BigDecimal(o.toString()); }
    private LocalDate toDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        return LocalDate.parse(o.toString());
    }
}
