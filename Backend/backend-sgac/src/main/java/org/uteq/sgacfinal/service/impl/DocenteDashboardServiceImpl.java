package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.response.DocenteDashboardDTO;
import org.uteq.sgacfinal.entity.Docente;
import org.uteq.sgacfinal.repository.DocenteDashboardRepository;
import org.uteq.sgacfinal.repository.DocenteRepository;
import org.uteq.sgacfinal.service.DocenteDashboardService;
import org.uteq.sgacfinal.service.IUsuarioSesionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocenteDashboardServiceImpl implements DocenteDashboardService {

    private final DocenteRepository docenteRepository;
    private final DocenteDashboardRepository dashboardRepository;
    private final IUsuarioSesionService usuarioSesionService;

    @Override
    public DocenteDashboardDTO obtenerResumen() {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        Docente docente = docenteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe docente asociado al usuario: " + idUsuario));

        Integer idDocente = docente.getIdDocente();

        Integer convocatoriasActivas = safeInt(dashboardRepository.countConvocatoriasActivas(idDocente));
        Integer postulacionesPendientes = safeInt(dashboardRepository.countPostulacionesPendientes(idDocente));
        Integer ayudantesAsignados = safeInt(dashboardRepository.countAyudantesAsignados(idDocente));
        Integer actividadesPorRevisar = safeInt(dashboardRepository.countActividadesPorRevisar(idDocente));

        List<DocenteDashboardDTO.UltimaActividadDTO> ultimas = dashboardRepository.findUltimasActividades(idDocente)
                .stream()
                .map(p -> DocenteDashboardDTO.UltimaActividadDTO.builder()
                        .fecha(p.getFecha())
                        .nombreEstudiante(p.getNombreEstudiante())
                        .tema(p.getTema())
                        .idRegistro(p.getIdRegistro())
                        .build())
                .toList();

        return DocenteDashboardDTO.builder()
                .totalConvocatoriasActivas(convocatoriasActivas)
                .totalPostulacionesPendientes(postulacionesPendientes)
                .totalAyudantesAsignados(ayudantesAsignados)
                .totalActividadesPorRevisar(actividadesPorRevisar)
                .ultimasActividades(ultimas)
                .build();
    }

    private Integer safeInt(Integer n) {
        return n != null ? n : 0;
    }
}
