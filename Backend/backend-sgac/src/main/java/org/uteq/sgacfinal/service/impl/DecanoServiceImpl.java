package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.response.*;
import org.uteq.sgacfinal.entity.Decano;
import org.uteq.sgacfinal.entity.LogAuditoria;
import org.uteq.sgacfinal.repository.DecanoRepository;
import org.uteq.sgacfinal.repository.LogAuditoriaRepository;
import org.uteq.sgacfinal.service.IDecanoService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DecanoServiceImpl implements IDecanoService {

    private final DecanoRepository decanoRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public DecanoResponseDTO crear(DecanoRequestDTO request) {
        Integer idGenerado = decanoRepository.registrarDecano(
                request.getIdUsuario(),
                request.getIdFacultad(),
                request.getFechaInicioGestion(),
                request.getFechaFinGestion()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar decano.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public DecanoResponseDTO actualizar(Integer id, DecanoRequestDTO request) {
        Integer resultado = decanoRepository.actualizarDecano(
                id,
                request.getIdFacultad(),
                request.getFechaInicioGestion(),
                request.getFechaFinGestion()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar decano.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = decanoRepository.desactivarDecano(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar decano.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DecanoResponseDTO buscarPorId(Integer id) {
        Decano decano = decanoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Decano no encontrado con ID: " + id));
        return mapearADTO(decano);
    }

    @Override
    @Transactional(readOnly = true)
    public DecanoResponseDTO buscarPorUsuario(Integer idUsuario) {
        Decano decano = decanoRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe decano activo para el usuario ID: " + idUsuario));
        return mapearADTO(decano);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecanoResponseDTO> listarActivos() {
        return decanoRepository.obtenerDecanosActivosSP().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DecanoEstadisticasDTO obtenerEstadisticasPorFacultad(Integer idFacultad) {
        log.info("Obteniendo estadísticas para la facultad ID: {}", idFacultad);
        String sql = "SELECT * FROM academico.fn_obtener_estadisticas_decano(?)";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                DecanoEstadisticasDTO dto = new DecanoEstadisticasDTO();
                dto.setTotalConvocatorias(rs.getLong("total_convocatorias"));
                dto.setConvocatoriasActivas(rs.getLong("convocatorias_activas"));
                dto.setConvocatoriasInactivas(rs.getLong("convocatorias_inactivas"));
                dto.setTotalPostulantes(rs.getLong("total_postulantes"));
                dto.setPostulantesSeleccionados(rs.getLong("postulantes_seleccionados"));
                dto.setPostulantesNoSeleccionados(rs.getLong("postulantes_no_seleccionados"));
                dto.setPostulantesEnEvaluacion(rs.getLong("postulantes_en_evaluacion"));
                dto.setPostulantesPendientes(rs.getLong("postulantes_pendientes"));

                String jsonActividad = rs.getString("actividad_coordinador");
                try {
                    if (jsonActividad != null && !jsonActividad.isEmpty()) {
                        List<DecanoEstadisticasDTO.ActividadCoordinadorDTO> actividad =
                                objectMapper.readValue(jsonActividad, new com.fasterxml.jackson.core.type.TypeReference<List<DecanoEstadisticasDTO.ActividadCoordinadorDTO>>() {});
                        dto.setActividadPorCoordinador(actividad);
                    } else {
                        dto.setActividadPorCoordinador(new ArrayList<>());
                    }
                } catch (Exception e) {
                    log.warn("Error al parsear actividad de coordinadores: {}", e.getMessage());
                    dto.setActividadPorCoordinador(new ArrayList<>());
                }
                return dto;
            }, idFacultad);
        } catch (Exception e) {
            log.error("Error al ejecutar fn_obtener_estadisticas_decano: {}", e.getMessage());
            return new DecanoEstadisticasDTO();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaReporteDTO> reporteConvocatoriasPorFacultad(Integer idFacultad) {
        log.info("Generando reporte de convocatorias para facultad ID: {}", idFacultad);
        String sql = "SELECT * FROM academico.fn_reporte_convocatorias_decano(?)";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            java.sql.Date fechaInicioSQL = rs.getDate("fecha_inicio");
            java.sql.Date fechaFinSQL = rs.getDate("fecha_fin");

            return ConvocatoriaReporteDTO.builder()
                    .idConvocatoria(rs.getInt("id_convocatoria"))
                    .nombreAsignatura(rs.getString("nombre_asignatura"))
                    .nombreCarrera(rs.getString("nombre_carrera"))
                    .nombreCoordinador(rs.getString("nombre_coordinador"))
                    .fechaInicio(fechaInicioSQL != null ? fechaInicioSQL.toLocalDate() : null)
                    .fechaFin(fechaFinSQL != null ? fechaFinSQL.toLocalDate() : null)
                    .estado(rs.getString("estado"))
                    .numeroPostulantes(rs.getLong("numero_postulantes"))
                    .build();
        }, idFacultad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorPostulanteReporteDTO> reportePostulantesPorFacultad(Integer idFacultad) {
        log.info("Generando reporte de postulantes para facultad ID: {}", idFacultad);
        String sql = "SELECT * FROM academico.fn_reporte_postulantes_decano(?)";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            java.sql.Date fechaPostSQL = rs.getDate("fecha_postulacion");

            return CoordinadorPostulanteReporteDTO.builder()
                    .idPostulacion(rs.getInt("id_postulacion"))
                    .nombreEstudiante(rs.getString("nombre_estudiante"))
                    .cedula(rs.getString("cedula"))
                    .nombreAsignatura(rs.getString("nombre_asignatura"))
                    .nombrePeriodo(rs.getString("nombre_periodo"))
                    .fechaPostulacion(fechaPostSQL != null ? fechaPostSQL.toLocalDate() : null)
                    .estadoEvaluacion(rs.getString("estado_evaluacion"))
                    .build();
        }, idFacultad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogAuditoriaDTO> reporteAuditoriaPorFacultad(Integer idFacultad) {
        List<LogAuditoria> logs = logAuditoriaRepository.findByFacultadCoordinadores(idFacultad);
        return logs.stream().map(l -> LogAuditoriaDTO.builder()
                .idLog(l.getIdLogAuditoria())
                .nombreUsuario(l.getUsuario().getNombres() + " " + l.getUsuario().getApellidos())
                .accion(l.getAccion())
                .tablaAfectada(l.getTablaAfectada())
                .fechaHora(l.getFechaHora())
                .build()).collect(Collectors.toList());
    }

    private DecanoResponseDTO mapearADTO(Decano entidad) {
        String nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();
        return DecanoResponseDTO.builder()
                .idDecano(entidad.getIdDecano())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(nombreUsuario)
                .idFacultad(entidad.getFacultad().getIdFacultad())
                .nombreFacultad(entidad.getFacultad().getNombreFacultad())
                .fechaInicioGestion(entidad.getFechaInicioGestion())
                .fechaFinGestion(entidad.getFechaFinGestion())
                .activo(entidad.getActivo())
                .build();
    }
}