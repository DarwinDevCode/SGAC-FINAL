package com.sgac.service;

import com.sgac.dto.CoordinadorDTO;
import com.sgac.dto.CoordinadorRequest;
import java.util.List;

public interface CoordinadorService {
    List<CoordinadorDTO> findAll();
    List<CoordinadorDTO> findByActivo(Boolean activo);
    CoordinadorDTO findById(Integer id);
    CoordinadorDTO create(CoordinadorRequest request);
    CoordinadorDTO update(Integer id, CoordinadorRequest request);
    void delete(Integer id);
}
