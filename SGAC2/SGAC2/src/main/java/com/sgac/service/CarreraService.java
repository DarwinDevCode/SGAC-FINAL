package com.sgac.service;

import com.sgac.dto.CarreraDTO;
import com.sgac.dto.CarreraRequest;
import java.util.List;

public interface CarreraService {
    List<CarreraDTO> findAll();
    List<CarreraDTO> findByFacultad(Integer idFacultad);
    CarreraDTO findById(Integer id);
    CarreraDTO create(CarreraRequest request);
    CarreraDTO update(Integer id, CarreraRequest request);
    void delete(Integer id);
}
