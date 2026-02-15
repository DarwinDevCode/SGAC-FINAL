package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoEstadoRequisitoRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRequisitoResponseDTO;
import java.util.List;

public interface ITipoEstadoRequisitoService {

    TipoEstadoRequisitoResponseDTO crear(TipoEstadoRequisitoRequestDTO request);

    TipoEstadoRequisitoResponseDTO actualizar(Integer id, TipoEstadoRequisitoRequestDTO request);

    void eliminar(Integer id);

    TipoEstadoRequisitoResponseDTO buscarPorId(Integer id);

    List<TipoEstadoRequisitoResponseDTO> listarTodos();
}