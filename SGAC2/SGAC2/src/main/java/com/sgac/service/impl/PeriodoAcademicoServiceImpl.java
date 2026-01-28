package com.sgac.service.impl;

import com.sgac.dto.PeriodoAcademicoDTO;
import com.sgac.dto.PeriodoAcademicoRequest;
import com.sgac.entity.PeriodoAcademico;
import com.sgac.exception.ResourceNotFoundException;
import com.sgac.repository.PeriodoAcademicoRepository;
import com.sgac.service.PeriodoAcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PeriodoAcademicoServiceImpl implements PeriodoAcademicoService {

    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoAcademicoDTO> findAll() {
        return periodoAcademicoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoAcademicoDTO> findByEstado(String estado) {
        return periodoAcademicoRepository.findByEstado(estado).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoAcademicoDTO findById(Integer id) {
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PeriodoAcademico", "id", id));
        return convertToDTO(periodo);
    }

    @Override
    public PeriodoAcademicoDTO create(PeriodoAcademicoRequest request) {
        PeriodoAcademico periodo = PeriodoAcademico.builder()
                .nombrePeriodo(request.getNombrePeriodo())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(request.getEstado())
                .build();
        return convertToDTO(periodoAcademicoRepository.save(periodo));
    }

    @Override
    public PeriodoAcademicoDTO update(Integer id, PeriodoAcademicoRequest request) {
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PeriodoAcademico", "id", id));
        
        periodo.setNombrePeriodo(request.getNombrePeriodo());
        periodo.setFechaInicio(request.getFechaInicio());
        periodo.setFechaFin(request.getFechaFin());
        periodo.setEstado(request.getEstado());
        return convertToDTO(periodoAcademicoRepository.save(periodo));
    }

    @Override
    public void delete(Integer id) {
        if (!periodoAcademicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("PeriodoAcademico", "id", id);
        }
        periodoAcademicoRepository.deleteById(id);
    }

    private PeriodoAcademicoDTO convertToDTO(PeriodoAcademico periodo) {
        return PeriodoAcademicoDTO.builder()
                .idPeriodoAcademico(periodo.getIdPeriodoAcademico())
                .nombrePeriodo(periodo.getNombrePeriodo())
                .fechaInicio(periodo.getFechaInicio())
                .fechaFin(periodo.getFechaFin())
                .estado(periodo.getEstado())
                .build();
    }
}
