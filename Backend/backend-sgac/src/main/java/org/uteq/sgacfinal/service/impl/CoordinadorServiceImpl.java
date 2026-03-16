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
import org.uteq.sgacfinal.entity.EvaluacionMeritos;
import org.uteq.sgacfinal.entity.EvaluacionOposicion;
import org.uteq.sgacfinal.entity.UsuarioComision;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        Coordinador coordinador = coordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coordinador no encontrado con ID: " + id));
        return mapearADTO(coordinador);
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorResponseDTO buscarPorUsuario(Integer idUsuario) {
        Coordinador coordinador = coordinadorRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe coordinador activo para el usuario ID: " + idUsuario));
        return mapearADTO(coordinador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorResponseDTO> listarTodos() {
        return coordinadorRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
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
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorConvocatoriaReporteDTO> reporteConvocatoriasPropias(Integer idUsuario) {
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
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorPostulanteReporteDTO> reportePostulantesPropios(Integer idUsuario) {
        List<Postulacion> postulaciones = postulacionRepository.findByCoordinadorPropioActivo(idUsuario);

        return postulaciones.stream().map(p -> {
            // Cálculo de Méritos
            BigDecimal totalMeritos = BigDecimal.ZERO;
            if (p.getEvaluacionesMeritos() != null && !p.getEvaluacionesMeritos().isEmpty()) {
                EvaluacionMeritos em = p.getEvaluacionesMeritos().get(0);
                if (em.getNotaAsignatura() != null) totalMeritos = totalMeritos.add(em.getNotaAsignatura());
                if (em.getNotaSemestres() != null) totalMeritos = totalMeritos.add(em.getNotaSemestres());
                if (em.getNotaEventos() != null) totalMeritos = totalMeritos.add(em.getNotaEventos());
                if (em.getNotaExperiencia() != null) totalMeritos = totalMeritos.add(em.getNotaExperiencia());
            }

            // Cálculo de Oposición (Promedio de los integrantes de la comisión)
            BigDecimal totalOposicion = BigDecimal.ZERO;
            if (p.getEvaluacionesOposicion() != null && !p.getEvaluacionesOposicion().isEmpty()) {
                EvaluacionOposicion eo = p.getEvaluacionesOposicion().get(0);
                List<UsuarioComision> integrantes = eo.getUsuariosComision();
                
                if (integrantes != null && !integrantes.isEmpty()) {
                    BigDecimal sumaOposicion = BigDecimal.ZERO;
                    int contIntegrantes = 0;
                    for (UsuarioComision uc : integrantes) {
                        BigDecimal puntajeIntegrante = BigDecimal.ZERO;
                        boolean tieneNotas = false;
                        if (uc.getPuntajeMaterial() != null) {
                            puntajeIntegrante = puntajeIntegrante.add(uc.getPuntajeMaterial());
                            tieneNotas = true;
                        }
                        if (uc.getPuntajeRespuestas() != null) {
                            puntajeIntegrante = puntajeIntegrante.add(uc.getPuntajeRespuestas());
                            tieneNotas = true;
                        }
                        if (uc.getPuntajeExposicion() != null) {
                            puntajeIntegrante = puntajeIntegrante.add(uc.getPuntajeExposicion());
                            tieneNotas = true;
                        }
                        
                        if (tieneNotas) {
                            sumaOposicion = sumaOposicion.add(puntajeIntegrante);
                            contIntegrantes++;
                        }
                    }
                    if (contIntegrantes > 0) {
                        totalOposicion = sumaOposicion.divide(new BigDecimal(contIntegrantes), 2, RoundingMode.HALF_UP);
                    }
                }
            }

            BigDecimal puntajeTotal = totalMeritos.add(totalOposicion);

            return CoordinadorPostulanteReporteDTO.builder()
                .idPostulacion(p.getIdPostulacion())
                .nombreEstudiante(p.getEstudiante() != null && p.getEstudiante().getUsuario() != null ? 
                        p.getEstudiante().getUsuario().getNombres() + " " + p.getEstudiante().getUsuario().getApellidos() : "Desconocido")
                .cedula(p.getEstudiante() != null && p.getEstudiante().getUsuario() != null ? p.getEstudiante().getUsuario().getCedula() : "N/A")
                .nombreAsignatura(p.getConvocatoria() != null && p.getConvocatoria().getAsignatura() != null ? 
                        p.getConvocatoria().getAsignatura().getNombreAsignatura() : "N/A")
                .nombrePeriodo(p.getConvocatoria() != null && p.getConvocatoria().getPeriodoAcademico() != null ? 
                        p.getConvocatoria().getPeriodoAcademico().getNombrePeriodo() : "N/A")
                .fechaPostulacion(p.getFechaPostulacion())
                .estadoEvaluacion(p.getEstadoPostulacion())
                .puntajeMeritos(totalMeritos)
                .puntajeOposicion(totalOposicion)
                .puntajeTotal(puntajeTotal)
                .observacionPostulacion(p.getObservaciones())
                .build();
        }).collect(Collectors.toList());
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
}