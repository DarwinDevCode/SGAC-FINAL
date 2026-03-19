package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.CoordinadorRequestDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorResponseDTO;
import org.uteq.sgacfinal.entity.Coordinador;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.CoordinadorRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.ICoordinadorService;
import org.uteq.sgacfinal.dto.Response.CoordinadorEstadisticasDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorPostulanteReporteDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CoordinadorServiceImpl implements ICoordinadorService {

    private final CoordinadorRepository coordinadorRepository;
    private final IConvocatoriaRepository convocatoriaRepository;
    private final PostulacionRepository postulacionRepository;

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
        return new CoordinadorResponseDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorResponseDTO buscarPorUsuario(Integer idUsuario) {
        return new CoordinadorResponseDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorResponseDTO> listarTodos() {
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorEstadisticasDTO obtenerEstadisticasPropias(Integer idUsuario) {
        // Obtener todas las postulaciones de las convocatorias de este coordinador
        List<Postulacion> postulaciones = postulacionRepository.findByCoordinadorPropioActivo(idUsuario);

        // Métricas de postulantes por estado (códigos reales de la BD)
        long totalPostulantes = postulaciones.size();
        // "aprobados" en el sistema = SELECCIONADO
        long aprobados = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && "SELECCIONADO".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo()))
                .count();
        // "rechazados" en el sistema = NO_SELECCIONADO
        long rechazados = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && "NO_SELECCIONADO".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo()))
                .count();
        // "en evaluación" = EN_EVALUACION + ASIGNADO + ELEGIBLE
        long enEvaluacion = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && ("EN_EVALUACION".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo())
                             || "ASIGNADO".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo())
                             || "ELEGIBLE".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo())))
                .count();
        // "pendientes" = PENDIENTE (recién postulado, sin revisión)
        long pendientes = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && "PENDIENTE".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo()))
                .count();

        // Agrupar postulaciones por convocatoria para métricas de convocatorias
        Map<Convocatoria, Long> porConvocatoria = postulaciones.stream()
                .collect(Collectors.groupingBy(Postulacion::getConvocatoria, Collectors.counting()));

        // Obtener las convocatorias únicas de las postulaciones
        List<Convocatoria> convocatoriasConPostulantes = new ArrayList<>(porConvocatoria.keySet());

        // Convocatorias propias (las de las postulaciones) + las activas del coordinador
        // Para el total y activas/inactivas, usamos convocatorias del coordinador directamente si existe la query;
        // si no, usamos las inferidas de las postulaciones
        long totalConvocatorias = convocatoriasConPostulantes.size();
        long activas = convocatoriasConPostulantes.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .count();
        long inactivas = totalConvocatorias - activas;

        // Top 5 convocatorias por cantidad de postulantes
        List<CoordinadorEstadisticasDTO.PostulantesPorConvocatoriaDTO> topConvocatorias = porConvocatoria.entrySet()
                .stream()
                .sorted(Map.Entry.<Convocatoria, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    String titulo = e.getKey().getAsignatura() != null
                            ? e.getKey().getAsignatura().getNombreAsignatura()
                            : "Sin título";
                    return new CoordinadorEstadisticasDTO.PostulantesPorConvocatoriaDTO(titulo, e.getValue());
                })
                .collect(Collectors.toList());

        return CoordinadorEstadisticasDTO.builder()
                .totalConvocatoriasPropias(totalConvocatorias)
                .convocatoriasActivas(activas)
                .convocatoriasInactivas(inactivas)
                .totalPostulantesRecibidos(totalPostulantes)
                .postulantesAprobados(aprobados)
                .postulantesRechazados(rechazados)
                .postulantesEnEvaluacion(enEvaluacion)
                .postulantesPendientes(pendientes)
                .postulantesPorConvocatoria(topConvocatorias)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorConvocatoriaReporteDTO> reporteConvocatoriasPropias(Integer idUsuario) {
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorPostulanteReporteDTO> reportePostulantesPropios(Integer idUsuario) {
        return new ArrayList<>();
    }
}