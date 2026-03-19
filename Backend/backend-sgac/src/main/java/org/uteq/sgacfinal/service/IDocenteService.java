package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.DocenteRequestDTO;
import org.uteq.sgacfinal.dto.response.DocenteResponseDTO;
import java.util.List;

public interface IDocenteService {

    DocenteResponseDTO crear(DocenteRequestDTO request);

    DocenteResponseDTO actualizar(Integer id, DocenteRequestDTO request);

    void desactivar(Integer id);

    DocenteResponseDTO buscarPorId(Integer id);

    DocenteResponseDTO buscarPorUsuario(Integer idUsuario);

    List<DocenteResponseDTO> listarTodos();

    List<DocenteResponseDTO> listarDocentesActivos();

}