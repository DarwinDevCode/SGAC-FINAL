package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.TipoRolDTO;
import org.uteq.sgacfinal.dto.TipoRolRequest;

import java.util.List;

public interface TipoRolService {

    List<TipoRolDTO> findAll();

    List<TipoRolDTO> findAllActive();

    TipoRolDTO findById(Integer id);

    TipoRolDTO create(TipoRolRequest request);

    TipoRolDTO update(Integer id, TipoRolRequest request);

    void delete(Integer id);

    void toggleActive(Integer id);
}
