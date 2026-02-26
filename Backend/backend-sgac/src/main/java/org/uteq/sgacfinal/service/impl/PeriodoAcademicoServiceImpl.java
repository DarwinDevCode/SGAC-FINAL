package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoResponseDTO;
import org.uteq.sgacfinal.entity.PeriodoAcademico;
import org.uteq.sgacfinal.repository.IPeriodoAcademicoRepository;
import org.uteq.sgacfinal.service.IPeriodoAcademicoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PeriodoAcademicoServiceImpl implements IPeriodoAcademicoService {

    private final IPeriodoAcademicoRepository periodoRepository;

    @Override
    public PeriodoAcademicoResponseDTO crear(PeriodoAcademicoRequestDTO request) {
        Integer idGenerado = periodoRepository.registrarPeriodo(
                request.getNombrePeriodo(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getEstado()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar periodo académico.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public PeriodoAcademicoResponseDTO actualizar(Integer id, PeriodoAcademicoRequestDTO request) {
        Integer resultado = periodoRepository.actualizarPeriodo(
                id,
                request.getNombrePeriodo(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getEstado()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar periodo académico.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = periodoRepository.desactivarPeriodo(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar periodo académico.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoAcademicoResponseDTO buscarPorId(Integer id) {
        PeriodoAcademico periodo = periodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periodo no encontrado con ID: " + id));
        return mapearADTO(periodo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoAcademicoResponseDTO> listarTodos() {
        return periodoRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoAcademicoResponseDTO obtenerPeriodoActivo() {
        return periodoRepository.findFirstByEstadoAndActivoTrueOrderByFechaInicioDesc("ACTIVO")
                .map(this::mapearADTO)
                .orElse(null);
    }


    private PeriodoAcademicoResponseDTO mapearADTO(PeriodoAcademico entidad) {
        return PeriodoAcademicoResponseDTO.builder()
                .idPeriodoAcademico(entidad.getIdPeriodoAcademico())
                .nombrePeriodo(entidad.getNombrePeriodo())
                .fechaInicio(entidad.getFechaInicio())
                .fechaFin(entidad.getFechaFin())
                .estado(entidad.getEstado())
                .build();
    }

//    @Override
//    @Transactional(readOnly = true)
//    public PeriodoAcademicoResponseDTO obtenerPeriodoActivo() {
//        PeriodoAcademico periodo = periodoRepository.findByEstado("ACTIVO")
//
//        return PeriodoAcademicoResponseDTO.builder()
//                .idPeriodoAcademico(periodo.getIdPeriodoAcademico())
//                .nombrePeriodo(periodo.getNombrePeriodo())
//                .fechaInicio(periodo.getFechaInicio())
//                .fechaFin(periodo.getFechaFin())
//                .estado(periodo.getEstado())
//                .build();
//    }
}