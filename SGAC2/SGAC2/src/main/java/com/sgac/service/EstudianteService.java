package com.sgac.service;

import com.sgac.dto.EstudianteDTO;
import com.sgac.dto.EstudianteRequest;
import java.util.List;

public interface EstudianteService {
    List<EstudianteDTO> findAll();
    List<EstudianteDTO> findByCarrera(Integer idCarrera);
    EstudianteDTO findById(Integer id);
    EstudianteDTO create(EstudianteRequest request);
    EstudianteDTO update(Integer id, EstudianteRequest request);
    void delete(Integer id);
}
