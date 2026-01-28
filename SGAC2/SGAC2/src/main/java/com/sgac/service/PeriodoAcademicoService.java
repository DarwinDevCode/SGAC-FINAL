package com.sgac.service;

import com.sgac.dto.PeriodoAcademicoDTO;
import com.sgac.dto.PeriodoAcademicoRequest;
import java.util.List;

public interface PeriodoAcademicoService {
    List<PeriodoAcademicoDTO> findAll();
    List<PeriodoAcademicoDTO> findByEstado(String estado);
    PeriodoAcademicoDTO findById(Integer id);
    PeriodoAcademicoDTO create(PeriodoAcademicoRequest request);
    PeriodoAcademicoDTO update(Integer id, PeriodoAcademicoRequest request);
    void delete(Integer id);
}
