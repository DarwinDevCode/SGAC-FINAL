package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.FacultadDTO;
import org.uteq.sgacfinal.dto.FacultadRequest;

import java.util.List;

public interface FacultadService {
    List<FacultadDTO> findAll();
    FacultadDTO findById(Integer id);
    FacultadDTO create(FacultadRequest request);
    FacultadDTO update(Integer id, FacultadRequest request);
    void delete(Integer id);
}
