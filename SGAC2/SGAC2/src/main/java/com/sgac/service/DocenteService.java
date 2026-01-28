package com.sgac.service;

import com.sgac.dto.DocenteDTO;
import com.sgac.dto.DocenteRequest;
import java.util.List;

public interface DocenteService {
    List<DocenteDTO> findAll();
    List<DocenteDTO> findByActivo(Boolean activo);
    DocenteDTO findById(Integer id);
    DocenteDTO create(DocenteRequest request);
    DocenteDTO update(Integer id, DocenteRequest request);
    void delete(Integer id);
}
