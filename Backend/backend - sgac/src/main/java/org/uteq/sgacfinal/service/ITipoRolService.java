package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoRolRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import java.util.List;

public interface ITipoRolService {

    TipoRolResponseDTO crear(TipoRolRequestDTO request);

    TipoRolResponseDTO actualizar(Integer id, TipoRolRequestDTO request);

    TipoRolResponseDTO buscarPorId(Integer id);

    TipoRolResponseDTO buscarPorNombre(String nombre);

    List<TipoRolResponseDTO> listarTodos();
}