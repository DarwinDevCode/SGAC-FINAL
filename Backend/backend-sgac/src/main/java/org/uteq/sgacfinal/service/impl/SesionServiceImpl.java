package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.EvidenciaRequest;
import org.uteq.sgacfinal.dto.Request.RegistrarSesionRequest;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.exception.AccesoDenegadoException;
import org.uteq.sgacfinal.exception.RecursoNoEncontradoException;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.repository.EvidenciaRegistroActividadRepository;
import org.uteq.sgacfinal.repository.ProgresoRepository;
import org.uteq.sgacfinal.repository.RegistroActividadRepository;
import org.uteq.sgacfinal.service.SesionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SesionServiceImpl implements SesionService {
    private final RegistroActividadRepository registroActividadRepository;
    private final EvidenciaRegistroActividadRepository evidenciaRepository;
    private final AyudantiaRepository ayudantiaRepository;
    private final ProgresoRepository progresoRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<SesionListadoResponse> listarSesiones(
            Integer idUsuario, LocalDate fechaDesde, LocalDate fechaHasta,
            String estado, Integer idPeriodo) {

        List<Object[]> resultados = registroActividadRepository
                .listarSesiones(idUsuario, fechaDesde, fechaHasta, estado, idPeriodo);

        return resultados.stream()
                .map(this::mapearSesionListado)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SesionDetalleResponse detalleSesion(Integer idUsuario, Integer idRegistro) {

        if (!registroActividadRepository.perteneceAlAyudante(idRegistro, idUsuario)) {
            throw new AccesoDenegadoException("No tiene acceso a este registro.");
        }

        Object[] fila = registroActividadRepository
                .detalleSesion(idUsuario, idRegistro)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Sesión no encontrada con id: " + idRegistro
                ));

        List<EvidenciaResponse> evidencias = evidenciasSesion(idUsuario, idRegistro);

        return mapearSesionDetalle(fila, evidencias);
    }

    @Override
    @Transactional
    public List<EvidenciaResponse> evidenciasSesion(Integer idUsuario, Integer idRegistro) {

        if (!registroActividadRepository.perteneceAlAyudante(idRegistro, idUsuario)) {
            throw new AccesoDenegadoException("No tiene acceso a este registro.");
        }

        List<Object[]> resultados = evidenciaRepository
                .evidenciasSesion(idUsuario, idRegistro);

        return resultados.stream()
                .map(this::mapearEvidencia)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProgresoGeneralResponse progresoGeneral(Integer idUsuario) {

        List<Object[]> resultado = progresoRepository.progresoGeneral(idUsuario);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No se encontró ayudantía activa para el usuario: " + idUsuario
            );
        }

        return mapearProgresoGeneral(resultado.get(0));
    }

    @Override
    @Transactional
    public ControlSemanalResponse controlSemanal(Integer idUsuario) {

        List<Object[]> resultado = progresoRepository.controlSemanal(idUsuario);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No se encontró ayudantía activa para el usuario: " + idUsuario
            );
        }

        return mapearControlSemanal(resultado.get(0));
    }


    @Override
    @Transactional
    public RegistrarSesionResponse registrarSesion(
            Integer idUsuario, RegistrarSesionRequest request) {

        Integer idAyudantia = ayudantiaRepository
                .findIdAyudantiaActivaByUsuario(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró ayudantía activa para el usuario."
                ));

        String evidenciasJson = serializarEvidencias(request.getEvidencias());

        Object[] resultado = registroActividadRepository.registrarActividad(
                idUsuario,
                idAyudantia,
                request.getDescripcionActividad(),
                request.getTemaTratado(),
                request.getFecha(),
                request.getNumeroAsistentes(),
                request.getHorasDedicadas(),
                evidenciasJson
        );

        return RegistrarSesionResponse.builder()
                .exito((Boolean) resultado[0])
                .mensaje((String) resultado[1])
                .idRegistroCreado(resultado[2] != null ? (Integer) resultado[2] : null)
                .build();
    }

    private SesionListadoResponse mapearSesionListado(Object[] fila) {
        return SesionListadoResponse.builder()
                .idRegistro((Integer)     fila[0])
                .fecha(toLocalDate(       fila[1]))
                .temaTratado((String)     fila[2])
                .descripcion((String)     fila[3])
                .numeroAsistentes((Integer) fila[4])
                .horasDedicadas(toBigDecimal(fila[5]))
                .estado((String)          fila[6])
                .totalEvidencias((Long)   fila[7])
                .tieneObservacion((Boolean) fila[8])
                .build();
    }

    private SesionDetalleResponse mapearSesionDetalle(
            Object[] fila, List<EvidenciaResponse> evidencias) {
        return SesionDetalleResponse.builder()
                .idRegistro((Integer)       fila[0])
                .fecha(toLocalDate(         fila[1]))
                .temaTratado((String)       fila[2])
                .descripcion((String)       fila[3])
                .numeroAsistentes((Integer) fila[4])
                .horasDedicadas(toBigDecimal(fila[5]))
                .estado((String)            fila[6])
                .nombreAsignatura((String)  fila[7])
                .nombreDocente((String)     fila[8])
                .nombrePeriodo((String)     fila[9])
                .evidencias(evidencias)
                .build();
    }

    private EvidenciaResponse mapearEvidencia(Object[] fila) {
        return EvidenciaResponse.builder()
                .idEvidencia((Integer)      fila[0])
                .nombreArchivo((String)     fila[1])
                .rutaArchivo((String)       fila[2])
                .mimeType((String)          fila[3])
                .tamanioBytes((Integer)     fila[4])
                .tipoEvidencia((String)     fila[5])
                .estadoEvidencia((String)   fila[6])
                .fechaSubida(toLocalDate(   fila[7]))
                .build();
    }

    private ProgresoGeneralResponse mapearProgresoGeneral(Object[] fila) {
        return ProgresoGeneralResponse.builder()
                .horasAprobadas(toBigDecimal(    fila[0]))
                .horasPendientes(toBigDecimal(   fila[1]))
                .horasObservadas(toBigDecimal(   fila[2]))
                .horasTotalesRegistradas(toBigDecimal(fila[3]))
                .horasMaximas(toBigDecimal(      fila[4]))
                .porcentajeAvance(toBigDecimal(  fila[5]))
                .totalSesiones((Long)            fila[6])
                .sesionesAprobadas((Long)        fila[7])
                .sesionesPendientes((Long)       fila[8])
                .sesionesObservadas((Long)       fila[9])
                .build();
    }

    private ControlSemanalResponse mapearControlSemanal(Object[] fila) {
        return ControlSemanalResponse.builder()
                .semanaInicio(toLocalDate(       fila[0]))
                .semanaFin(toLocalDate(          fila[1]))
                .horasRegistradas(toBigDecimal(  fila[2]))
                .horasAprobadasSemana(toBigDecimal(fila[3]))
                .horasPendientesSemana(toBigDecimal(fila[4]))
                .limiteSemanal(toBigDecimal(     fila[5]))
                .horasDisponibles(toBigDecimal(  fila[6]))
                .superaLimite((Boolean)          fila[7])
                .sesionesSemana((Long)           fila[8])
                .build();
    }

    private LocalDate toLocalDate(Object valor) {
        if (valor == null) return null;
        if (valor instanceof java.sql.Date d) return d.toLocalDate();
        return LocalDate.parse(valor.toString());
    }

    private BigDecimal toBigDecimal(Object valor) {
        if (valor == null) return BigDecimal.ZERO;
        if (valor instanceof BigDecimal bd) return bd;
        return new BigDecimal(valor.toString());
    }

    private String serializarEvidencias(List<EvidenciaRequest> evidencias) {
        try {
            return objectMapper.writeValueAsString(
                    evidencias.stream().map(e -> Map.of(
                            "id_tipo_evidencia", e.getIdTipoEvidencia(),
                            "nombre_archivo",    e.getNombreArchivo(),
                            "ruta_archivo",      e.getRutaArchivo(),
                            "mime_type",         e.getMimeType()     != null ? e.getMimeType()     : "",
                            "tamanio_bytes",     e.getTamanioBytes() != null ? e.getTamanioBytes() : 0
                    )).collect(Collectors.toList())
            );
        } catch (JsonProcessingException ex) {
            log.error("Error al serializar evidencias: {}", ex.getMessage());
            throw new RuntimeException("Error al procesar las evidencias.");
        }
    }
}
