package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.AyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.AyudanteCatedraResponseDTO;
import java.util.List;

public interface IAyudanteCatedraService {

    AyudanteCatedraResponseDTO crear(AyudanteCatedraRequestDTO request);

    AyudanteCatedraResponseDTO actualizar(Integer id, AyudanteCatedraRequestDTO request);

    AyudanteCatedraResponseDTO buscarPorId(Integer id);

    AyudanteCatedraResponseDTO buscarPorUsuario(Integer idUsuario);

    List<AyudanteCatedraResponseDTO> listarTodos();
}