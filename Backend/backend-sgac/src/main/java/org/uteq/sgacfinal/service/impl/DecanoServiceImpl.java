package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.response.DecanoResponseDTO;
import org.uteq.sgacfinal.entity.Decano;
import org.uteq.sgacfinal.entity.LogAuditoria;
import org.uteq.sgacfinal.repository.DecanoRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.repository.LogAuditoriaRepository;
import org.uteq.sgacfinal.service.IDecanoService;
import org.uteq.sgacfinal.dto.response.DecanoEstadisticasDTO;
import org.uteq.sgacfinal.dto.response.ConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.response.LogAuditoriaDTO;

import java.util.ArrayList;
import java.util.List;
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
        /*
        List<Convocatoria> convocatorias = convocatoriaRepository.findByFacultadPropia(idFacultad);
        List<Postulacion> postulaciones = postulacionRepository.findByFacultadPropia(idFacultad);

        long totalConvocatorias = convocatorias.size();
        long activas = convocatorias.stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).count();
        long inactivas = totalConvocatorias - activas;

        long totalPostulantes = postulaciones.size();
        long aprobados = postulaciones.stream().filter(p -> "APROBADO".equalsIgnoreCase(p.getEstadoPostulacion())).count();
        long rechazados = postulaciones.stream().filter(p -> "RECHAZADO".equalsIgnoreCase(p.getEstadoPostulacion())).count();
        long enEvaluacion = postulaciones.stream().filter(p -> "EN_EVALUACION".equalsIgnoreCase(p.getEstadoPostulacion()) || "ASIGNADO".equalsIgnoreCase(p.getEstadoPostulacion())).count();

        // Agrupar convocatorias por nombre del coordinador activo de cada carrera
        Map<String, Long> actividadPorCoord = convocatorias.stream()
            .collect(Collectors.groupingBy(
                c -> c.getAsignatura().getCarrera().getCoordinadores().stream()
                        .filter(coord -> Boolean.TRUE.equals(coord.getActivo()))
                        .findFirst()
                        .map(coord -> coord.getUsuario().getNombres() + " " + coord.getUsuario().getApellidos())
                        .orElse("Sin Coordinador Asignado"),
                Collectors.counting()
            ));

        List<DecanoEstadisticasDTO.ActividadCoordinadorDTO> actividadList = actividadPorCoord.entrySet().stream()
            .map(e -> new DecanoEstadisticasDTO.ActividadCoordinadorDTO(e.getKey(), e.getValue()))
            .collect(Collectors.toList());

        return DecanoEstadisticasDTO.builder()
                .totalConvocatorias(totalConvocatorias)
                .convocatoriasActivas(activas)
                .convocatoriasInactivas(inactivas)
                .totalPostulantes(totalPostulantes)
                .postulantesAprobados(aprobados)
                .postulantesRechazados(rechazados)
                .postulantesEnEvaluacion(enEvaluacion)
                .actividadPorCoordinador(actividadList)
                .build();

         */
        return new DecanoEstadisticasDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaReporteDTO> reporteConvocatoriasPorFacultad(Integer idFacultad) {

        /*
        List<Convocatoria> convocatorias = convocatoriaRepository.findByFacultadPropia(idFacultad);
        // Podríamos optimizar esto, pero como es un reporte con volumen moderado, está bien.
        return convocatorias.stream().map(c -> {
            long postulantes = c.getPostulaciones() != null ? c.getPostulaciones().size() : 0L;
            
            String nombreCoord = c.getAsignatura().getCarrera().getCoordinadores().stream()
                    .filter(coord -> Boolean.TRUE.equals(coord.getActivo()))
                    .findFirst()
                    .map(coord -> coord.getUsuario().getNombres() + " " + coord.getUsuario().getApellidos())
                    .orElse("Sin Coordinador Asignado");

            return ConvocatoriaReporteDTO.builder()
                    .idConvocatoria(c.getIdConvocatoria())
                    .nombreAsignatura(c.getAsignatura().getNombreAsignatura())
                    .nombreCarrera(c.getAsignatura().getCarrera().getNombreCarrera())
                    .nombreCoordinador(nombreCoord)
                    .fechaInicio(c.getFechaPublicacion())
                    .fechaFin(c.getFechaCierre())
                    .estado(Boolean.TRUE.equals(c.getActivo()) ? "ACTIVO" : "INACTIVO")
                    .numeroPostulantes(postulantes)
                    .build();
        }).collect(Collectors.toList());

         */
        return new ArrayList<>();
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