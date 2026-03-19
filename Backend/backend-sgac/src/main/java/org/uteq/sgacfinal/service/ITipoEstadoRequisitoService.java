package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.TipoEstadoRequisitoRequestDTO;
import org.uteq.sgacfinal.dto.response.TipoEstadoRequisitoResponseDTO;
import java.util.List;

public interface ITipoEstadoRequisitoService {
    List<TipoEstadoRequisitoResponseDTO> listarTodos();
    TipoEstadoRequisitoResponseDTO crear(TipoEstadoRequisitoRequestDTO request);
    TipoEstadoRequisitoResponseDTO actualizar(Integer id, TipoEstadoRequisitoRequestDTO request);
    void desactivar(Integer id);
}