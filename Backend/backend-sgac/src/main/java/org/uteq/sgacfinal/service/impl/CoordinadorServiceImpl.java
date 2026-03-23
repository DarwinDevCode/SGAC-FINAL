package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.uteq.sgacfinal.dto.request.CoordinadorRequestDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorEstadisticasDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorPostulanteReporteDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorResponseDTO;
import org.uteq.sgacfinal.entity.Coordinador;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.CoordinadorRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.ICoordinadorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
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
        /*
        List<Convocatoria> convocatorias = convocatoriaRepository.findByCoordinadorPropio(idUsuario);

        long totalConvocatorias = convocatorias.size();
        long activas = convocatorias.stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).count();
        long inactivas = totalConvocatorias - activas;

        List<Postulacion> todasLasPostulaciones = convocatorias.stream()
                .flatMap(c -> c.getPostulaciones().stream())
                .collect(Collectors.toList());

        long totalPostulantes = todasLasPostulaciones.size();
        long aprobados = todasLasPostulaciones.stream().filter(p -> "APROBADO".equalsIgnoreCase(p.getEstadoPostulacion())).count();
        long rechazados = todasLasPostulaciones.stream().filter(p -> "RECHAZADO".equalsIgnoreCase(p.getEstadoPostulacion())).count();
        long enEvaluacion = todasLasPostulaciones.stream().filter(p -> "EN_EVALUACION".equalsIgnoreCase(p.getEstadoPostulacion()) || "ASIGNADO".equalsIgnoreCase(p.getEstadoPostulacion())).count();

        List<CoordinadorEstadisticasDTO.PostulantesPorConvocatoriaDTO> topConvocatorias = convocatorias.stream()
                .filter(c -> c.getPostulaciones() != null && !c.getPostulaciones().isEmpty())
                .map(c -> new CoordinadorEstadisticasDTO.PostulantesPorConvocatoriaDTO(
                        c.getAsignatura().getNombreAsignatura(),
                        (long) c.getPostulaciones().size()
                ))
                .sorted(Comparator.comparing(CoordinadorEstadisticasDTO.PostulantesPorConvocatoriaDTO::getCantidadPostulantes).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return CoordinadorEstadisticasDTO.builder()
                .totalConvocatoriasPropias(totalConvocatorias)
                .convocatoriasActivas(activas)
                .convocatoriasInactivas(inactivas)
                .totalPostulantesRecibidos(totalPostulantes)
                .postulantesAprobados(aprobados)
                .postulantesRechazados(rechazados)
                .postulantesEnEvaluacion(enEvaluacion)
                .postulantesPorConvocatoria(topConvocatorias)
                .build();

        */
        return new CoordinadorEstadisticasDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorConvocatoriaReporteDTO> reporteConvocatoriasPropias(Integer idUsuario) {

        /*
        List<Convocatoria> convocatorias = convocatoriaRepository.findByCoordinadorPropio(idUsuario);

        return convocatorias.stream().map(c -> CoordinadorConvocatoriaReporteDTO.builder()
                .idConvocatoria(c.getIdConvocatoria())
                .nombreAsignatura(c.getAsignatura().getNombreAsignatura())
                .nombreCarrera(c.getAsignatura().getCarrera().getNombreCarrera())
                .nombrePeriodo(c.getPeriodoAcademico().getNombrePeriodo())
                .fechaInicio(c.getFechaPublicacion())
                .fechaFin(c.getFechaCierre())
                .cuposAprobados(c.getCuposDisponibles())
                .estado(Boolean.TRUE.equals(c.getActivo()) ? "ACTIVO" : "INACTIVO")
                .numeroPostulantes((long) (c.getPostulaciones() != null ? c.getPostulaciones().size() : 0))
                .build()
        ).collect(Collectors.toList());
         */
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorPostulanteReporteDTO> reportePostulantesPropios(Integer idUsuario) {
        /*
        List<Postulacion> postulaciones = postulacionRepository.findByCoordinadorPropioActivo(idUsuario);

        return postulaciones.stream().map(p -> CoordinadorPostulanteReporteDTO.builder()
                .idPostulacion(p.getIdPostulacion())
                .nombreEstudiante(p.getEstudiante().getUsuario().getNombres() + " " + p.getEstudiante().getUsuario().getApellidos())
                .cedula(p.getEstudiante().getUsuario().getCedula())
                .nombreAsignatura(p.getConvocatoria().getAsignatura().getNombreAsignatura())
                .nombrePeriodo(p.getConvocatoria().getPeriodoAcademico().getNombrePeriodo())
                .fechaPostulacion(p.getFechaPostulacion())
                .estadoEvaluacion(p.getEstadoPostulacion())
                .build()
        ).collect(Collectors.toList());
    }

    private CoordinadorResponseDTO mapearADTO(Coordinador entidad) {
        String nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();
        return CoordinadorResponseDTO.builder()
                .idCoordinador(entidad.getIdCoordinador())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(nombreUsuario)
                .cedula(entidad.getUsuario().getCedula())
                .idCarrera(entidad.getCarrera().getIdCarrera())
                .nombreCarrera(entidad.getCarrera().getNombreCarrera())
                .fechaInicio(entidad.getFechaInicio())
                .fechaFin(entidad.getFechaFin())
                .activo(entidad.getActivo())
                .build();
    }
    */
        return new ArrayList<>();
    }
}