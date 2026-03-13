package org.uteq.sgacfinal.service.impl.estudiante;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaPostulacionDTO;
import org.uteq.sgacfinal.dto.Response.DetallePostulacionResponseDTO;
import org.uteq.sgacfinal.dto.Response.estudiante.EstudianteDashboardResponseDTO;
import org.uteq.sgacfinal.dto.Response.PostulacionInfoDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.CronogramaActivoResponseDTO;
import org.uteq.sgacfinal.entity.Estudiante;
import org.uteq.sgacfinal.repository.estudiante.EstudianteDashboardRepository;
import org.uteq.sgacfinal.repository.EstudianteRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.repository.configuracion.ICronogramaRepository;
import org.uteq.sgacfinal.service.estudiante.EstudianteDashboardService;
import org.uteq.sgacfinal.service.IUsuarioSesionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstudianteDashboardServiceImpl implements EstudianteDashboardService {

    private final EstudianteRepository estudianteRepository;
    private final EstudianteDashboardRepository dashboardRepository;
    private final PostulacionRepository postulacionRepository;
    private final ICronogramaRepository cronogramaRepository;
    private final IUsuarioSesionService usuarioSesionService;
    private final ObjectMapper objectMapper;

    @Override
    public EstudianteDashboardResponseDTO obtenerResumen() {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        Estudiante estudiante = estudianteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe estudiante asociado al usuario: " + idUsuario));

        Integer idEstudiante = estudiante.getIdEstudiante();

        Integer convocatoriasAbiertas = safeInt(
                dashboardRepository.countConvocatoriasAbiertasReales(idUsuario)
        );

        Integer misPostulaciones = safeInt(
                dashboardRepository.countMisPostulaciones(idEstudiante)
        );

        CronogramaActivoResponseDTO cronogramaActivo = obtenerCronogramaActivoReal();
        DetallePostulacionResponseDTO detallePostulacion = obtenerMiPostulacionReal(idUsuario);

        String faseActual = "Sin fase activa";
        String periodoAcademico = "Sin periodo activo";
        EstudianteDashboardResponseDTO.ResumenProcesoDTO resumenProceso = null;
        EstudianteDashboardResponseDTO.UltimaPostulacionDTO ultimaPostulacion = null;

        /*
         * CRONOGRAMA REAL:
         * Sale de planificacion.fn_obtener_cronograma_activo(),
         * exactamente igual que la pantalla de cronograma activo.
         */
        if (cronogramaActivo != null
                && cronogramaActivo.isExito()
                && cronogramaActivo.getPeriodo() != null) {

            LocalDate fechaInicioPeriodo = parseLocalDate(cronogramaActivo.getPeriodo().getFechaInicio());
            LocalDate fechaFinPeriodo = parseLocalDate(cronogramaActivo.getPeriodo().getFechaFin());

            String nombrePeriodo = cronogramaActivo.getPeriodo().getNombre();
            if (nombrePeriodo != null && !nombrePeriodo.isBlank()) {
                periodoAcademico = nombrePeriodo;
            }

            LocalDate fechaInicioFaseActual = null;
            LocalDate fechaFinFaseActual = null;

            if (cronogramaActivo.getFases() != null) {
                Optional<CronogramaActivoResponseDTO.FaseInfoDTO> faseActualOpt = cronogramaActivo.getFases()
                        .stream()
                        .filter(f -> Boolean.TRUE.equals(f.getEsActual()))
                        .findFirst();

                if (faseActualOpt.isPresent()) {
                    CronogramaActivoResponseDTO.FaseInfoDTO fase = faseActualOpt.get();
                    faseActual = fase.getNombre() != null ? fase.getNombre() : "Sin fase activa";
                    fechaInicioFaseActual = parseLocalDate(fase.getFechaInicio());
                    fechaFinFaseActual = parseLocalDate(fase.getFechaFin());
                }
            }

            Integer porcentajeAvance = 0;
            if (cronogramaActivo.getPeriodo().getPorcentajeAvance() != null) {
                porcentajeAvance = cronogramaActivo.getPeriodo()
                        .getPorcentajeAvance()
                        .setScale(0, RoundingMode.HALF_UP)
                        .intValue();
            }

            resumenProceso = EstudianteDashboardResponseDTO.ResumenProcesoDTO.builder()
                    .fechaInicioPeriodo(fechaInicioPeriodo)
                    .fechaFinPeriodo(fechaFinPeriodo)
                    .fechaInicioFaseActual(fechaInicioFaseActual)
                    .fechaFinFaseActual(fechaFinFaseActual)
                    .porcentajeAvance(porcentajeAvance)
                    .build();
        }

        /*
         * MI POSTULACIÓN REAL:
         * Sale de postulacion.fn_ver_detalle_postulacion(idUsuario),
         * exactamente igual que la pantalla "Mi Postulación".
         */
        if (detallePostulacion != null
                && Boolean.TRUE.equals(detallePostulacion.getExito())
                && detallePostulacion.getPostulacion() != null
                && detallePostulacion.getConvocatoria() != null) {

            PostulacionInfoDTO post = detallePostulacion.getPostulacion();
            ConvocatoriaPostulacionDTO conv = detallePostulacion.getConvocatoria();

            ultimaPostulacion = EstudianteDashboardResponseDTO.UltimaPostulacionDTO.builder()
                    .idPostulacion(post.getIdPostulacion())
                    .asignatura(conv.getNombreAsignatura())
                    .docente(conv.getNombreDocente())
                    .fechaPostulacion(post.getFechaPostulacion())
                    .estado(post.getEstadoNombre())
                    .observacion(post.getObservaciones())
                    .build();
        }

        return EstudianteDashboardResponseDTO.builder()
                .convocatoriasAbiertas(convocatoriasAbiertas)
                .misPostulaciones(misPostulaciones)
                .faseActual(faseActual)
                .periodoAcademico(periodoAcademico)
                .resumenProceso(resumenProceso)
                .ultimaPostulacion(ultimaPostulacion)
                .build();
    }

    private CronogramaActivoResponseDTO obtenerCronogramaActivoReal() {
        try {
            String json = cronogramaRepository.obtenerCronogramaActivo();

            if (json == null || json.isBlank()) {
                return null;
            }

            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private DetallePostulacionResponseDTO obtenerMiPostulacionReal(Integer idUsuario) {
        try {
            String json = postulacionRepository.obtenerDetallePostulacion(idUsuario);

            if (json == null || json.isBlank()) {
                return null;
            }

            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        return LocalDate.parse(fecha);
    }

    private Integer safeInt(Integer value) {
        return value != null ? value : 0;
    }
}