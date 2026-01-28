package com.sgac.service;

import com.sgac.dto.FacultadDTO;
import com.sgac.dto.FacultadRequest;
import java.util.List;

public interface FacultadService {
    List<FacultadDTO> findAll();
    FacultadDTO findById(Integer id);
    FacultadDTO create(FacultadRequest request);
    FacultadDTO update(Integer id, FacultadRequest request);
    void delete(Integer id);
}
