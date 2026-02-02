package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.Response.DecanoResponseDTO;
import java.util.List;

public interface IDecanoService {

    DecanoResponseDTO crear(DecanoRequestDTO request);

    DecanoResponseDTO actualizar(Integer id, DecanoRequestDTO request);

    void desactivar(Integer id);

    DecanoResponseDTO buscarPorId(Integer id);

    DecanoResponseDTO buscarPorUsuario(Integer idUsuario);

    List<DecanoResponseDTO> listarActivos();
}