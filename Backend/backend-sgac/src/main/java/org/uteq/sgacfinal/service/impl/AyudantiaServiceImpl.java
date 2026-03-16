package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.AyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.AyudantiaDetalleResponseDTO;
import org.uteq.sgacfinal.dto.Response.AyudantiaResponseDTO;
import org.uteq.sgacfinal.dto.Response.EvidenciaDetalleDTO;
import org.uteq.sgacfinal.dto.Response.EvidenciaResponseDTO;
import org.uteq.sgacfinal.dto.Response.RegistroActividadDetalleDTO;
import org.uteq.sgacfinal.dto.Response.SesionResponseDTO;
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
        Ayudantia ayudantia = ayudantiaRepository.findById(idAyudantia)
                .orElseThrow(() -> new ResourceNotFoundException("Ayudantía no encontrada"));

        Integer idUsuario = ayudantia.getPostulacion().getEstudiante().getUsuario().getIdUsuario();
        
        // Usamos el procedimiento almacenado para listar las sesiones del ayudante
        List<Object[]> rows = ayudantiaRepository.findAllByAyudanteId(idUsuario);

        List<RegistroActividadDetalleDTO> actividades = rows.stream()
                .map(row -> {
                    Integer idRegistro = (Integer) row[0];
                    // Para cada actividad, recuperamos sus evidencias vía SP
                    List<Object[]> evRows = ayudantiaRepository.findDetalleConEvidenciasById(idUsuario, idRegistro);
                    
                    List<EvidenciaDetalleDTO> evidencias = evRows.stream()
                            .map(evRow -> EvidenciaDetalleDTO.builder()
                                    .idEvidencia((Integer) evRow[0])
                                    .nombreArchivo((String) evRow[1])
                                    .rutaArchivo((String) evRow[2])
                                    .build())
                            .collect(Collectors.toList());

                    return RegistroActividadDetalleDTO.builder()
                            .idRegistroActividad(idRegistro)
                            .temaTratado((String) row[2])
                            .descripcionActividad((String) row[3])
                            .fecha(toLocalDate(row[1]))
                            .horasDedicadas(toBigDecimal(row[5]))
                            .estadoRevision((String) row[6])
                            .evidencias(evidencias)
                            .build();
                })
                .collect(Collectors.toList());

        return AyudantiaDetalleResponseDTO.builder()
                .idAyudantia(ayudantia.getIdAyudantia())
                .fechaInicio(ayudantia.getFechaInicio())
                .fechaFin(ayudantia.getFechaFin())
                .horasCumplidas(ayudantia.getHorasCumplidas())
                .actividades(actividades)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroActividadDetalleDTO> listarActividadesPorUsuario(Integer idUsuario) {
        List<Object[]> rawData = ayudantiaRepository.findActividadesRawByUsuario(idUsuario);

        return rawData.stream().map(row -> {
            return RegistroActividadDetalleDTO.builder()
                    .idRegistroActividad((Integer) row[0])
                    .temaTratado((String) row[2])
                    .descripcionActividad((String) row[3])
                    .fecha(toLocalDate(row[1]))
                    .horasDedicadas(toBigDecimal(row[5]))
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
        // Índices según fn_listar_sesiones:
        // 0 id_registro
        // 1 fecha
        // 2 tema_tratado
        // 3 descripcion
        // 4 numero_asistentes
        // 5 horas_dedicadas
        // 6 estado
        // 7 total_evidencias
        // 8 tiene_observacion

        String nombreEstado = row[6] != null ? row[6].toString() : null;
        Boolean tieneObs = row[8] != null && (Boolean) row[8];

        return SesionResponseDTO.builder()
                .idRegistroActividad((Integer) row[0])
                .descripcionActividad((String) row[3])
                .temaTratado((String) row[2])
                .fecha(toLocalDate(row[1]))
                .numeroAsistentes((Integer) row[4])
                .horasDedicadas(toBigDecimal(row[5]))
                // mantenemos estadoRevision por compatibilidad
                .estadoRevision(nombreEstado)
                .nombreEstado(nombreEstado)
                .tieneObservacion(tieneObs)
                .observacionDocente(tieneObs ? "OBSERVADO" : "Sin observaciones")
                .build();
    }

    private EvidenciaResponseDTO mapearEvidenciaDetalleRow(Object[] row) {
        // Índices según fn_evidencias_sesion:
        // 0 id_evidencia
        // 1 nombre_archivo
        // 2 ruta_archivo
        // 3 mime_type
        // 4 tamanio_bytes
        // 5 tipo_evidencia
        // 6 estado_evidencia
        // 7 fecha_subida

        if (row[0] == null) {
            return null;
        }

        String nombreEstadoEvidencia = row[6] != null ? row[6].toString() : null;

        return EvidenciaResponseDTO.builder()
                .idEvidenciaRegistroActividad((Integer) row[0])
                .nombreArchivo((String) row[1])
                .rutaArchivo((String) row[2])
                .mimeType((String) row[3])
                .tamanioBytes((Integer) row[4])
                .fechaSubida(toLocalDate(row[7]))
                .tipoEvidencia((String) row[5])
                .estadoEvidencia(nombreEstadoEvidencia)
                .nombreEstadoEvidencia(nombreEstadoEvidencia)
                .build();
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        if (value instanceof java.sql.Date d) return d.toLocalDate();
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return LocalDate.parse(value.toString());
    }

    private BigDecimal toBigDecimal(Object valor) {
        if (valor == null) return BigDecimal.ZERO;
        if (valor instanceof BigDecimal bd) return bd;
        return new BigDecimal(valor.toString());
    }
}
