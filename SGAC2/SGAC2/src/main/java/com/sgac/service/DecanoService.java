package com.sgac.service;

import com.sgac.dto.DecanoDTO;
import com.sgac.dto.DecanoRequest;
import java.util.List;

public interface DecanoService {
    List<DecanoDTO> findAll();
    List<DecanoDTO> findByActivo(Boolean activo);
    DecanoDTO findById(Integer id);
    DecanoDTO create(DecanoRequest request);
    DecanoDTO update(Integer id, DecanoRequest request);
    void delete(Integer id);
}
