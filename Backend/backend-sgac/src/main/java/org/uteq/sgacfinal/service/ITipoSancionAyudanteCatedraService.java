package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.TipoSancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.response.TipoSancionAyudanteCatedraResponseDTO;
import java.util.List;

public interface ITipoSancionAyudanteCatedraService {
    List<TipoSancionAyudanteCatedraResponseDTO> listarTodos();
    List<TipoSancionAyudanteCatedraResponseDTO> listarActivos();
    TipoSancionAyudanteCatedraResponseDTO crear(TipoSancionAyudanteCatedraRequestDTO request);
    TipoSancionAyudanteCatedraResponseDTO actualizar(Integer id, TipoSancionAyudanteCatedraRequestDTO request);
    void desactivar(Integer id);
}