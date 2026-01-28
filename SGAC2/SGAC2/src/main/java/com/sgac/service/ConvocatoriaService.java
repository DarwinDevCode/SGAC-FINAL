package com.sgac.service;

import com.sgac.dto.ConvocatoriaDTO;
import com.sgac.dto.ConvocatoriaRequest;
import java.util.List;

public interface ConvocatoriaService {
    List<ConvocatoriaDTO> findAll();
    List<ConvocatoriaDTO> findByActivo(Boolean activo);
    List<ConvocatoriaDTO> findByPeriodo(Integer idPeriodoAcademico);
    ConvocatoriaDTO findById(Integer id);
    ConvocatoriaDTO create(ConvocatoriaRequest request);
    ConvocatoriaDTO update(Integer id, ConvocatoriaRequest request);
    void delete(Integer id);
}
