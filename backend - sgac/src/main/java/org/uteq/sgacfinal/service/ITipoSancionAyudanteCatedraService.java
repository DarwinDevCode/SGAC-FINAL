package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoSancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoSancionAyudanteCatedraResponseDTO;
import java.util.List;

public interface ITipoSancionAyudanteCatedraService {

    TipoSancionAyudanteCatedraResponseDTO crear(TipoSancionAyudanteCatedraRequestDTO request);

    TipoSancionAyudanteCatedraResponseDTO actualizar(Integer id, TipoSancionAyudanteCatedraRequestDTO request);

    void eliminar(Integer id);

    TipoSancionAyudanteCatedraResponseDTO buscarPorId(Integer id);

    List<TipoSancionAyudanteCatedraResponseDTO> listarTodos();
}