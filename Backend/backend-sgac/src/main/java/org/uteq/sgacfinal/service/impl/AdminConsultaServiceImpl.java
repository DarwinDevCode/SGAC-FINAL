package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.response.AdminConsultaDTO;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.entity.LogAuditoria;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.IPeriodoAcademicoRepository;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.LogAuditoriaRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.IAdminConsultaService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminConsultaServiceImpl implements IAdminConsultaService {

    private final IUsuariosRepository usuariosRepository;
    private final IConvocatoriaRepository convocatoriaRepository;
    private final PostulacionRepository postulacionRepository;
    private final IPeriodoAcademicoRepository periodoRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;

    @Override
    public AdminConsultaDTO obtenerResumenSistema() {

        // --- KPIs ---
        List<Usuario> usuarios = usuariosRepository.findAllWithRolesAndTipoRol();
        List<Convocatoria> convocatorias = convocatoriaRepository.findAll();
        List<Postulacion> postulaciones = postulacionRepository.findAll();

        long totalUsuarios = usuarios.size();
        long totalConvocatorias = convocatorias.size();
        long totalPostulaciones = postulaciones.size();

        String periodoActivo = periodoRepository
                .findFirstByEstadoAndActivoTrueOrderByFechaInicioDesc("ACTIVO")
                .map(p -> p.getNombrePeriodo())
                .orElse("Sin periodo activo");

        // --- Estadisticas mensuales (últimos 6 meses) ---
        List<AdminConsultaDTO.EstadisticaMensualDTO> estadisticasMensuales = calcularEstadisticasMensuales(convocatorias, postulaciones);

        // --- Distribución por roles ---
        List<AdminConsultaDTO.RolEstadisticaDTO> distribucionRoles = calcularDistribucionRoles(usuarios);

        // --- Distribución por facultad ---
        List<AdminConsultaDTO.FacultadEstadisticaDTO> distribucionFacultades = calcularDistribucionFacultades(postulaciones);

        // --- Últimas acciones (10) ---
        List<LogAuditoria> logs = logAuditoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(LogAuditoria::getFechaHora, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());

        List<AdminConsultaDTO.LogActividadDTO> ultimasAcciones = logs.stream()
                .map(l -> AdminConsultaDTO.LogActividadDTO.builder()
                        .idLog(l.getIdLogAuditoria())
                        .usuario(l.getUsuario().getNombres() + " " + l.getUsuario().getApellidos())
                        .accion(l.getAccion())
                        .modulo(l.getTablaAfectada())
                        .fecha(l.getFechaHora() != null ? l.getFechaHora().toString() : "")
                        .build())
                .collect(Collectors.toList());

        return AdminConsultaDTO.builder()
                .totalUsuarios(totalUsuarios)
                .totalConvocatorias(totalConvocatorias)
                .totalPostulaciones(totalPostulaciones)
                .periodoActivo(periodoActivo)
                .estadisticasMensuales(estadisticasMensuales)
                .distribucionRoles(distribucionRoles)
                .distribucionFacultades(distribucionFacultades)
                .ultimasAcciones(ultimasAcciones)
                .build();
    }

    private List<AdminConsultaDTO.EstadisticaMensualDTO> calcularEstadisticasMensuales(
            List<Convocatoria> convocatorias, List<Postulacion> postulaciones) {

        /*
        List<AdminConsultaDTO.EstadisticaMensualDTO> resultado = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate mes = hoy.minusMonths(i);
            int anio = mes.getYear();
            int mesNum = mes.getMonthValue();
            String nombreMes = mes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "EC"));

            long convsMes = convocatorias.stream()
                    .filter(c -> c.getFechaPublicacion() != null
                            && c.getFechaPublicacion().getYear() == anio
                            && c.getFechaPublicacion().getMonthValue() == mesNum)
                    .count();

            long postsMes = postulaciones.stream()
                    .filter(p -> p.getFechaPostulacion() != null
                            && p.getFechaPostulacion().getYear() == anio
                            && p.getFechaPostulacion().getMonthValue() == mesNum)
                    .count();

            resultado.add(new AdminConsultaDTO.EstadisticaMensualDTO(nombreMes, postsMes, convsMes));
        }

        return resultado;

         */

        List<AdminConsultaDTO.EstadisticaMensualDTO> result = new ArrayList<>();



        return new ArrayList<>();
    }

    private List<AdminConsultaDTO.RolEstadisticaDTO> calcularDistribucionRoles(List<Usuario> usuarios) {
        Map<String, Long> conteo = new HashMap<>();
        for (Usuario u : usuarios) {
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                String rol = u.getRoles().get(0).getTipoRol() != null
                        ? u.getRoles().get(0).getTipoRol().getNombreTipoRol()
                        : "DESCONOCIDO";
                conteo.merge(rol.replace("ROLE_", ""), 1L, Long::sum);
            }
        }
        return conteo.entrySet().stream()
                .map(e -> new AdminConsultaDTO.RolEstadisticaDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(AdminConsultaDTO.RolEstadisticaDTO::getCantidad).reversed())
                .collect(Collectors.toList());
    }

    private List<AdminConsultaDTO.FacultadEstadisticaDTO> calcularDistribucionFacultades(List<Postulacion> postulaciones) {
        Map<String, Long> conteo = new HashMap<>();
        for (Postulacion p : postulaciones) {
            try {
                String facultad = p.getConvocatoria().getAsignatura().getCarrera().getFacultad().getNombreFacultad();
                conteo.merge(facultad, 1L, Long::sum);
            } catch (NullPointerException ignored) {}
        }
        return conteo.entrySet().stream()
                .map(e -> new AdminConsultaDTO.FacultadEstadisticaDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(AdminConsultaDTO.FacultadEstadisticaDTO::getCantidadPostulaciones).reversed())
                .collect(Collectors.toList());
    }
}
