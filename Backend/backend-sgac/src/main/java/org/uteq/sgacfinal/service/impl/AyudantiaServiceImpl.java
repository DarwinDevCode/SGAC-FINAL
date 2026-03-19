package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.AyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.response.AyudantiaDetalleResponseDTO;
import org.uteq.sgacfinal.dto.response.AyudantiaResponseDTO;
import org.uteq.sgacfinal.dto.response.EvidenciaDetalleDTO;
import org.uteq.sgacfinal.dto.response.EvidenciaResponseDTO;
import org.uteq.sgacfinal.dto.response.RegistroActividadDetalleDTO;
import org.uteq.sgacfinal.dto.response.SesionResponseDTO;
import org.uteq.sgacfinal.entity.Ayudantia;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.service.IAyudantiaService;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AyudantiaServiceImpl implements IAyudantiaService {

    private final AyudantiaRepository ayudantiaRepository;

    @Override
    public AyudantiaResponseDTO crear(AyudantiaRequestDTO request) {
        Integer idGenerado = ayudantiaRepository.registrarAyudantia(
                request.getIdTipoEstadoEvidenciaAyudantia(),
                request.getIdPostulacion(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getHorasCumplidas()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al crear la ayudantía.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public AyudantiaResponseDTO actualizar(Integer id, AyudantiaRequestDTO request) {
        Integer resultado = ayudantiaRepository.actualizarAyudantia(
                id,
                request.getIdTipoEstadoEvidenciaAyudantia(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getHorasCumplidas()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la ayudantía.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AyudantiaResponseDTO buscarPorId(Integer id) {
        Ayudantia ayudantia = ayudantiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ayudantía no encontrada con ID: " + id));
        return mapearADTO(ayudantia);
    }

    @Override
    @Transactional(readOnly = true)
    public AyudantiaResponseDTO buscarPorPostulacion(Integer idPostulacion) {
        Ayudantia ayudantia = ayudantiaRepository.findById(idPostulacion)
                .orElseThrow(() -> new RuntimeException("No existe ayudantía para la postulación: " + idPostulacion));
        return mapearADTO(ayudantia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AyudantiaResponseDTO> listarTodas() {
        return ayudantiaRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private AyudantiaResponseDTO mapearADTO(Ayudantia entidad) {
        String nombreEstudiante = "";
        if(entidad.getPostulacion() != null && entidad.getPostulacion().getEstudiante() != null) {
            var usuario = entidad.getPostulacion().getEstudiante().getUsuario();
            nombreEstudiante = usuario.getNombres() + " " + usuario.getApellidos();
        }

        return AyudantiaResponseDTO.builder()
                .idAyudantia(entidad.getIdAyudantia())
                .nombreEstadoEvidencia(entidad.getIdTipoEstadoAyudantia().getNombreEstado())
                .idPostulacion(entidad.getPostulacion().getIdPostulacion())
                .nombreEstudiante(nombreEstudiante)
                .fechaInicio(entidad.getFechaInicio())
                .fechaFin(entidad.getFechaFin())
                .horasCumplidas(entidad.getHorasCumplidas())
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public AyudantiaDetalleResponseDTO obtenerDetallescompletos(Integer idAyudantia) {
        Ayudantia ayudantia = ayudantiaRepository.findAyudantiaConDetalles(idAyudantia)
                .orElseThrow(() -> new ResourceNotFoundException("Ayudantía no encontrada"));

        return AyudantiaDetalleResponseDTO.builder()
                .idAyudantia(ayudantia.getIdAyudantia())
                .fechaInicio(ayudantia.getFechaInicio())
                .fechaFin(ayudantia.getFechaFin())
                .horasCumplidas(ayudantia.getHorasCumplidas())
                .actividades(ayudantia.getRegistrosActividad().stream()
                        .map(ra -> RegistroActividadDetalleDTO.builder()
                                .idRegistroActividad(ra.getIdRegistroActividad())
                                .temaTratado(ra.getTemaTratado())
                                .fecha(ra.getFecha())
                                .horasDedicadas(ra.getHorasDedicadas())
                                .estadoRevision(ra.getIdTipoEstadoRegistro().getNombreEstado()) // Ajustado al esquema
                                .evidencias(ra.getEvidencias().stream()
                                        .map(ev -> EvidenciaDetalleDTO.builder()
                                                .idEvidencia(ev.getIdEvidenciaRegistroActividad())
                                                .nombreArchivo(ev.getNombreArchivo())
                                                .rutaArchivo(ev.getRutaArchivo())
                                                .build()).collect(Collectors.toList()))
                                .build()).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroActividadDetalleDTO> listarActividadesPorUsuario(Integer idUsuario) {
        List<Object[]> rawData = ayudantiaRepository.findActividadesRawByUsuario(idUsuario);

        return rawData.stream().map(row -> {
            return RegistroActividadDetalleDTO.builder()
                    .idRegistroActividad((Integer) row[0])
                    .descripcionActividad((String) row[2])
                    .temaTratado((String) row[3])
                    .fecha(row[4] != null ? ((Date) row[4]).toLocalDate() : null)
                    .horasDedicadas((BigDecimal) row[6])
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionResponseDTO> listarSesionesPorAyudante(Integer idAyudante) {
        List<Object[]> rows = ayudantiaRepository.findAllByAyudanteId(idAyudante);
        return rows.stream()
                .map(this::mapearSesionListadoRow)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SesionResponseDTO> obtenerDetalleSesionConEvidencias(Integer idAyudante, Integer idRegistroActividad) {
        List<Object[]> rows = ayudantiaRepository.findDetalleConEvidenciasById(idAyudante, idRegistroActividad);

        // Si no hay filas, puede ser que no exista la actividad o no pertenezca al ayudante
        if (rows == null || rows.isEmpty()) {
            return Optional.empty();
        }

        List<EvidenciaResponseDTO> evidencias = rows.stream()
                .map(this::mapearEvidenciaDetalleRow)
                .filter(Objects::nonNull)
                .toList();

        // La cabecera (actividad) ya está en el listado principal, aquí retornamos solo evidencias.
        return Optional.of(SesionResponseDTO.builder()
                .idRegistroActividad(idRegistroActividad)
                .evidencias(evidencias)
                .build());
    }

    private SesionResponseDTO mapearSesionListadoRow(Object[] row) {
        // Índices según SELECT de findAllByAyudanteId:
        // 0 id_registro_actividad
        // 1 descripcion_actividad
        // 2 tema_tratado
        // 3 fecha
        // 4 numero_asistentes
        // 5 horas_dedicadas
        // 6 id_tipo_estado_registro
        // 7 nombre_estado
        // 8 observaciones
        // 9 fecha_observacion

        Integer idTipoEstadoRegistro = row[6] != null ? ((Number) row[6]).intValue() : null;
        String nombreEstado = row[7] != null ? row[7].toString() : null;

        String observacion = (row[8] != null && !row[8].toString().isBlank())
                ? row[8].toString()
                : "Sin observaciones";

        LocalDate fechaObs = toLocalDate(row[9]);

        return SesionResponseDTO.builder()
                .idRegistroActividad(row[0] != null ? ((Number) row[0]).intValue() : null)
                .descripcionActividad((String) row[1])
                .temaTratado((String) row[2])
                .fecha(toLocalDate(row[3]))
                .numeroAsistentes(row[4] != null ? ((Number) row[4]).intValue() : null)
                .horasDedicadas((BigDecimal) row[5])
                .idTipoEstadoRegistro(idTipoEstadoRegistro)
                // mantenemos estadoRevision por compatibilidad (mismo valor que nombreEstado)
                .estadoRevision(nombreEstado)
                .nombreEstado(nombreEstado)
                .observacionDocente(observacion)
                .fechaObservacion(fechaObs)
                .evidencias(null)
                .build();
    }

    private EvidenciaResponseDTO mapearEvidenciaDetalleRow(Object[] row) {
        // Índices según SELECT de findDetalleConEvidenciasById (solo evidencias):
        // 0 id_evidencia_registro_actividad
        // 1 nombre_archivo
        // 2 ruta_archivo
        // 3 mime_type
        // 4 tamanio_bytes
        // 5 fecha_subida
        // 6 id_tipo_estado_evidencia
        // 7 nombre_estado_evidencia
        // 8 observacion_evidencia
        // 9 fecha_observacion_evidencia

        if (row[0] == null) {
            return null;
        }

        Integer idTipoEstadoEvidencia = row[6] != null ? ((Number) row[6]).intValue() : null;
        String nombreEstadoEvidencia = row[7] != null ? row[7].toString() : null;

        String observacion = (row[8] != null && !row[8].toString().isBlank())
                ? row[8].toString()
                : "Sin observaciones";

        return EvidenciaResponseDTO.builder()
                .idEvidenciaRegistroActividad(((Number) row[0]).intValue())
                .nombreArchivo((String) row[1])
                .rutaArchivo((String) row[2])
                .mimeType((String) row[3])
                .tamanioBytes(row[4] != null ? ((Number) row[4]).intValue() : null)
                .fechaSubida(toLocalDate(row[5]))

                // Ya no se envía tipo de evidencia (se infiere por mimeType)
                .idTipoEvidencia(null)
                .tipoEvidencia(null)

                .idTipoEstadoEvidencia(idTipoEstadoEvidencia)
                .estadoEvidencia(nombreEstadoEvidencia) // compatibilidad
                .nombreEstadoEvidencia(nombreEstadoEvidencia)
                .observacionDocente(observacion)
                .fechaObservacion(toLocalDate(row[9]))
                .build();
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return LocalDate.parse(value.toString());
    }
}
