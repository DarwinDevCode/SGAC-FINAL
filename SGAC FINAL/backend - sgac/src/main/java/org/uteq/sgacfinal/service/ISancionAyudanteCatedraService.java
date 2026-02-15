package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.SancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.SancionAyudanteCatedraResponseDTO;
import java.util.List;

public interface ISancionAyudanteCatedraService {

    SancionAyudanteCatedraResponseDTO crear(SancionAyudanteCatedraRequestDTO request);

    SancionAyudanteCatedraResponseDTO actualizar(Integer id, SancionAyudanteCatedraRequestDTO request);

    void desactivar(Integer id);

    SancionAyudanteCatedraResponseDTO buscarPorId(Integer id);

    List<SancionAyudanteCatedraResponseDTO> listarPorAyudante(Integer idAyudante);
}