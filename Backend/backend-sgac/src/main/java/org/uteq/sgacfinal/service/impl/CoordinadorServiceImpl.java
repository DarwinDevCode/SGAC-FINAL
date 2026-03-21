package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.CoordinadorRequestDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorResponseDTO;
import org.uteq.sgacfinal.repository.CoordinadorRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.ICoordinadorService;
import org.uteq.sgacfinal.dto.response.CoordinadorEstadisticasDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorPostulanteReporteDTO;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
@Transactional
public class CoordinadorServiceImpl implements ICoordinadorService {

    private final CoordinadorRepository coordinadorRepository;
    private final IConvocatoriaRepository convocatoriaRepository;
    private final PostulacionRepository postulacionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CoordinadorResponseDTO crear(CoordinadorRequestDTO request) {
        Integer idGenerado = coordinadorRepository.registrarCoordinador(
                request.getIdUsuario(),
                request.getIdCarrera(),
                request.getFechaInicio(),
                request.getFechaFin()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar coordinador.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public CoordinadorResponseDTO actualizar(Integer id, CoordinadorRequestDTO request) {
        Integer resultado = coordinadorRepository.actualizarCoordinador(
                id,
                request.getIdCarrera(),
                request.getFechaInicio(),
                request.getFechaFin()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar coordinador.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = coordinadorRepository.desactivarCoordinador(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar coordinador.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorResponseDTO buscarPorId(Integer id) {
        /*
        Coordinador coordinador = coordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coordinador no encontrado con ID: " + id));
        return mapearADTO(coordinador);

         */
        return new CoordinadorResponseDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorResponseDTO buscarPorUsuario(Integer idUsuario) {
        /*
        Coordinador coordinador = coordinadorRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe coordinador activo para el usuario ID: " + idUsuario));
        return mapearADTO(coordinador);
        */
        return new CoordinadorResponseDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorResponseDTO> listarTodos() {
        /*
        return coordinadorRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());

         */
        return new ArrayList<>();
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<CoordinadorResponseDTO> listarActivosPorCarrera(Integer idCarrera) {
//        return coordinadorRepository.findByCarrera_IdCarreraAndActivoTrue(idCarrera).stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorEstadisticasDTO obtenerEstadisticasPropias(Integer idUsuario) {
        String sql = "SELECT * FROM academico.fn_obtener_estadisticas_coordinador(?)";
        
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            CoordinadorEstadisticasDTO dto = new CoordinadorEstadisticasDTO();
            dto.setTotalConvocatoriasPropias(rs.getLong("total_convocatorias_propias"));
            dto.setConvocatoriasActivas(rs.getLong("convocatorias_activas"));
            dto.setConvocatoriasInactivas(rs.getLong("convocatorias_inactivas"));
            dto.setTotalPostulantesRecibidos(rs.getLong("total_postulantes_recibidos"));
            dto.setPostulantesAprobados(rs.getLong("postulantes_aprobados"));
            dto.setPostulantesRechazados(rs.getLong("postulantes_rechazados"));
            dto.setPostulantesEnEvaluacion(rs.getLong("postulantes_en_evaluacion"));
            dto.setPostulantesPendientes(rs.getLong("postulantes_pendientes"));
            
            String jsonTop = rs.getString("top_convocatorias");
            try {
                if (jsonTop != null && !jsonTop.isEmpty()) {
                    List<CoordinadorEstadisticasDTO.PostulantesPorConvocatoriaDTO> topList = 
                        objectMapper.readValue(jsonTop, new TypeReference<List<CoordinadorEstadisticasDTO.PostulantesPorConvocatoriaDTO>>() {});
                    dto.setPostulantesPorConvocatoria(topList);
                } else {
                    dto.setPostulantesPorConvocatoria(new ArrayList<>());
                }
            } catch (Exception e) {
                dto.setPostulantesPorConvocatoria(new ArrayList<>());
            }
            return dto;
        }, idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorConvocatoriaReporteDTO> reporteConvocatoriasPropias(Integer idUsuario) {
        String sql = "SELECT * FROM academico.fn_reporte_convocatorias_coordinador(?)";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Date fechaInicioSQL = rs.getDate("fecha_inicio");
            Date fechaFinSQL = rs.getDate("fecha_fin");
            
            java.time.LocalDate fechaInicio = fechaInicioSQL != null ? fechaInicioSQL.toLocalDate() : null;
            java.time.LocalDate fechaFin = fechaFinSQL != null ? fechaFinSQL.toLocalDate() : null;

            return CoordinadorConvocatoriaReporteDTO.builder()
                    .idConvocatoria(rs.getInt("id_convocatoria"))
                    .nombreAsignatura(rs.getString("nombre_asignatura"))
                    .nombreCarrera(rs.getString("nombre_carrera"))
                    .nombrePeriodo(rs.getString("nombre_periodo"))
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .cuposAprobados(rs.getInt("cupos_aprobados"))
                    .estado(rs.getString("estado"))
                    .numeroPostulantes(rs.getLong("numero_postulantes"))
                    .build();
        }, idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorPostulanteReporteDTO> reportePostulantesPropios(Integer idUsuario) {
        String sql = "SELECT * FROM academico.fn_reporte_postulantes_coordinador(?)";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Date fechaPostSQL = rs.getDate("fecha_postulacion");
            
            return CoordinadorPostulanteReporteDTO.builder()
                    .idPostulacion(rs.getInt("id_postulacion"))
                    .nombreEstudiante(rs.getString("nombre_estudiante"))
                    .cedula(rs.getString("cedula"))
                    .nombreAsignatura(rs.getString("nombre_asignatura"))
                    .nombrePeriodo(rs.getString("nombre_periodo"))
                    .fechaPostulacion(fechaPostSQL != null ? fechaPostSQL.toLocalDate() : null)
                    .estadoEvaluacion(rs.getString("estado_evaluacion"))
                    .build();
        }, idUsuario);
    }
}