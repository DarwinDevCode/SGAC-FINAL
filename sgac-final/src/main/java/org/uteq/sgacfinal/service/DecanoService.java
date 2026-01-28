package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.DecanoDTO;
import org.uteq.sgacfinal.dto.DecanoRequest;

import java.util.List;

public interface DecanoService {
    List<DecanoDTO> findAll();
    List<DecanoDTO> findByActivo(Boolean activo);
    DecanoDTO findById(Integer id);
    DecanoDTO create(DecanoRequest request);
    DecanoDTO update(Integer id, DecanoRequest request);
    void delete(Integer id);
}
