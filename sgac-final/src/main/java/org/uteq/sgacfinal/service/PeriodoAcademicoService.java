package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.PeriodoAcademicoDTO;
import org.uteq.sgacfinal.dto.PeriodoAcademicoRequest;

import java.util.List;

public interface PeriodoAcademicoService {
    List<PeriodoAcademicoDTO> findAll();
    List<PeriodoAcademicoDTO> findByEstado(String estado);
    PeriodoAcademicoDTO findById(Integer id);
    PeriodoAcademicoDTO create(PeriodoAcademicoRequest request);
    PeriodoAcademicoDTO update(Integer id, PeriodoAcademicoRequest request);
    void delete(Integer id);
}
