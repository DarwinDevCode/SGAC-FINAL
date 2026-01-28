package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.CoordinadorDTO;
import org.uteq.sgacfinal.dto.CoordinadorRequest;

import java.util.List;

public interface CoordinadorService {
    List<CoordinadorDTO> findAll();
    List<CoordinadorDTO> findByActivo(Boolean activo);
    CoordinadorDTO findById(Integer id);
    CoordinadorDTO create(CoordinadorRequest request);
    CoordinadorDTO update(Integer id, CoordinadorRequest request);
    void delete(Integer id);
}
