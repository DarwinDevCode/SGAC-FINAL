package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.Response.DecanoResponseDTO;
import org.uteq.sgacfinal.entity.Decano;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.entity.LogAuditoria;
import org.uteq.sgacfinal.repository.DecanoRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.repository.LogAuditoriaRepository;
import org.uteq.sgacfinal.service.IDecanoService;
import org.uteq.sgacfinal.dto.Response.DecanoEstadisticasDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorPostulanteReporteDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DecanoServiceImpl implements IDecanoService {

    private final DecanoRepository decanoRepository;
    private final IConvocatoriaRepository convocatoriaRepository;
    private final PostulacionRepository postulacionRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;

    @Override
    public DecanoResponseDTO crear(DecanoRequestDTO request) {
        Integer idGenerado = decanoRepository.registrarDecano(
                request.getIdUsuario(),
                request.getIdFacultad(),
                request.getFechaInicioGestion(),
                request.getFechaFinGestion()
        );
        if (idGenerado == -1) throw new RuntimeException("Error al registrar decano.");
        return buscarPorId(idGenerado);
    }

    @Override
    public DecanoResponseDTO actualizar(Integer id, DecanoRequestDTO request) {
        Integer resultado = decanoRepository.actualizarDecano(
                id, request.getIdFacultad(),
                request.getFechaInicioGestion(), request.getFechaFinGestion()
        );
        if (resultado == -1) throw new RuntimeException("Error al actualizar decano.");
        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = decanoRepository.desactivarDecano(id);
        if (resultado == -1) throw new RuntimeException("Error al desactivar decano.");
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
        // Postulaciones del periodo activo de esta facultad
        List<Postulacion> postulaciones = postulacionRepository.findByFacultadPropia(idFacultad);

        long totalPostulantes = postulaciones.size();

        // Seleccionados (equivalente a "aprobados" en la nomenclatura del sistema)
        long seleccionados = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && "SELECCIONADO".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo()))
                .count();

        // No Seleccionados (rechazados)
        long noSeleccionados = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && "NO_SELECCIONADO".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo()))
                .count();

        // En Evaluación (EN_EVALUACION + ASIGNADO + ELEGIBLE)
        long enEvaluacion = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && ("EN_EVALUACION".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo())
                             || "ASIGNADO".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo())
                             || "ELEGIBLE".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo())))
                .count();

        // Pendientes (recién postulados)
        long pendientes = postulaciones.stream()
                .filter(p -> p.getTipoEstadoPostulacion() != null
                        && "PENDIENTE".equalsIgnoreCase(p.getTipoEstadoPostulacion().getCodigo()))
                .count();

        // Convocatorias únicas de las postulaciones para métricas de convocatorias
        List<Convocatoria> convocatoriasUnicas = postulaciones.stream()
                .map(Postulacion::getConvocatoria)
                .distinct()
                .collect(Collectors.toList());

        long totalConvocatorias = convocatoriasUnicas.size();
        long activas = convocatoriasUnicas.stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).count();
        long inactivas = totalConvocatorias - activas;

        // Actividad por coordinador: agrupamos las postulaciones por el coordinador activo de la carrera
        Map<String, Long> actividadMap = postulaciones.stream()
                .collect(Collectors.groupingBy(p -> {
                    try {
                        return p.getConvocatoria().getAsignatura().getCarrera()
                                .getCoordinadores().stream()
                                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                                .findFirst()
                                .map(c -> c.getUsuario().getNombres() + " " + c.getUsuario().getApellidos())
                                .orElse("Sin Coordinador");
                    } catch (Exception e) {
                        return "Sin Coordinador";
                    }
                }, Collectors.counting()));

        List<DecanoEstadisticasDTO.ActividadCoordinadorDTO> actividadList = actividadMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new DecanoEstadisticasDTO.ActividadCoordinadorDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return DecanoEstadisticasDTO.builder()
                .totalConvocatorias(totalConvocatorias)
                .convocatoriasActivas(activas)
                .convocatoriasInactivas(inactivas)
                .totalPostulantes(totalPostulantes)
                .postulantesSeleccionados(seleccionados)
                .postulantesNoSeleccionados(noSeleccionados)
                .postulantesEnEvaluacion(enEvaluacion)
                .postulantesPendientes(pendientes)
                .actividadPorCoordinador(actividadList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaReporteDTO> reporteConvocatoriasPorFacultad(Integer idFacultad) {
        List<Convocatoria> convocatorias = convocatoriaRepository.findByFacultad(idFacultad);
        return convocatorias.stream().map(c -> {
            String coordinador = c.getAsignatura().getCarrera().getCoordinadores().stream()
                    .filter(coord -> Boolean.TRUE.equals(coord.getActivo()))
                    .findFirst()
                    .map(coord -> coord.getUsuario().getNombres() + " " + coord.getUsuario().getApellidos())
                    .orElse("Sin Asignar");

            return ConvocatoriaReporteDTO.builder()
                    .idConvocatoria(c.getIdConvocatoria())
                    .nombreAsignatura(c.getAsignatura().getNombreAsignatura())
                    .nombreCarrera(c.getAsignatura().getCarrera().getNombreCarrera())
                    .nombreCoordinador(coordinador)
                    .fechaInicio(c.getPeriodoAcademico().getFechaInicio())
                    .fechaFin(c.getPeriodoAcademico().getFechaFin())
                    .estado(Boolean.TRUE.equals(c.getActivo()) ? "ACTIVO" : "INACTIVO")
                    .numeroPostulantes((long) c.getPostulaciones().size())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorPostulanteReporteDTO> reportePostulantesPorFacultad(Integer idFacultad) {
        List<Postulacion> postulaciones = postulacionRepository.findByFacultadPropia(idFacultad);
        return postulaciones.stream().map(p -> CoordinadorPostulanteReporteDTO.builder()
                .idPostulacion(p.getIdPostulacion())
                .nombreEstudiante(p.getEstudiante().getUsuario().getNombres() + " " + p.getEstudiante().getUsuario().getApellidos())
                .cedula(p.getEstudiante().getUsuario().getCedula())
                .nombreAsignatura(p.getConvocatoria().getAsignatura().getNombreAsignatura())
                .nombrePeriodo(p.getConvocatoria().getPeriodoAcademico().getNombre())
                .fechaPostulacion(p.getFechaPostulacion())
                .estadoEvaluacion(p.getTipoEstadoPostulacion() != null ? p.getTipoEstadoPostulacion().getNombre() : "PENDIENTE")
                .build()).collect(Collectors.toList());
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