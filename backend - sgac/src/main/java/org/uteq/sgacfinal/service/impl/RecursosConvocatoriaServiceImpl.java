package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.AsignaturaResponseDTO;
import org.uteq.sgacfinal.dto.Response.DocenteResponseDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoResponseDTO;
import org.uteq.sgacfinal.entity.Asignatura;
import org.uteq.sgacfinal.entity.Docente;
import org.uteq.sgacfinal.entity.PeriodoAcademico;
import org.uteq.sgacfinal.repository.IAsignaturaRepository;
import org.uteq.sgacfinal.repository.DocenteRepository;
import org.uteq.sgacfinal.repository.IPeriodoAcademicoRepository;
import org.uteq.sgacfinal.service.IRecursosConvocatoriaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecursosConvocatoriaServiceImpl implements IRecursosConvocatoriaService {

    private final DocenteRepository docenteRepository;
    private final IAsignaturaRepository asignaturaRepository;
    private final IPeriodoAcademicoRepository periodoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DocenteResponseDTO> obtenerDocentesParaSelector() {
        List<Docente> docentes = docenteRepository.listarDocentesActivosConUsuario();
        return docentes.stream().map(docente -> {
            String nombreCompleto = docente.getUsuario().getNombres() + " " + docente.getUsuario().getApellidos();
            return DocenteResponseDTO.builder()
                    .idDocente(docente.getIdDocente())
                    .idUsuario(docente.getUsuario().getIdUsuario())
                    .nombreCompletoUsuario(nombreCompleto)
                    .activo(docente.getActivo())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignaturaResponseDTO> obtenerAsignaturasParaSelector() {
        List<Asignatura> asignaturas = asignaturaRepository.listarAsignaturasConCarrera();
        return asignaturas.stream().map(asignatura -> AsignaturaResponseDTO.builder()
                .idAsignatura(asignatura.getIdAsignatura())
                .nombreAsignatura(asignatura.getNombreAsignatura())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoAcademicoResponseDTO obtenerPeriodoActivo() {
        PeriodoAcademico periodo = periodoRepository.findByEstado("ACTIVO")
                .stream()
                .findFirst()
                .orElse(null);

        if (periodo == null)
            return null;

        return PeriodoAcademicoResponseDTO.builder()
                .idPeriodoAcademico(periodo.getIdPeriodoAcademico())
                .nombrePeriodo(periodo.getNombrePeriodo())
                .fechaInicio(periodo.getFechaInicio())
                .fechaFin(periodo.getFechaFin())
                .estado(periodo.getEstado())
                .build();
    }
}