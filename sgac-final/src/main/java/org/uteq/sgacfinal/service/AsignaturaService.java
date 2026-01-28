package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.AsignaturaDTO;
import org.uteq.sgacfinal.dto.AsignaturaRequest;

import java.util.List;

public interface AsignaturaService {
    List<AsignaturaDTO> findAll();
    List<AsignaturaDTO> findByCarrera(Integer idCarrera);
    AsignaturaDTO findById(Integer id);
    AsignaturaDTO create(AsignaturaRequest request);
    AsignaturaDTO update(Integer id, AsignaturaRequest request);
    void delete(Integer id);
}
