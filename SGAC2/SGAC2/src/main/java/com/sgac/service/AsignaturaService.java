package com.sgac.service;

import com.sgac.dto.AsignaturaDTO;
import com.sgac.dto.AsignaturaRequest;
import java.util.List;

public interface AsignaturaService {
    List<AsignaturaDTO> findAll();
    List<AsignaturaDTO> findByCarrera(Integer idCarrera);
    AsignaturaDTO findById(Integer id);
    AsignaturaDTO create(AsignaturaRequest request);
    AsignaturaDTO update(Integer id, AsignaturaRequest request);
    void delete(Integer id);
}
