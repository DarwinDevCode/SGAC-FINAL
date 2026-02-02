package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.AsignaturaRequestDTO;
import org.uteq.sgacfinal.dto.Response.AsignaturaResponseDTO;
import java.util.List;

public interface IAsignaturaService {

    AsignaturaResponseDTO crear(AsignaturaRequestDTO request);
    AsignaturaResponseDTO actualizar(Integer id, AsignaturaRequestDTO request);
    void desactivar(Integer id);
    AsignaturaResponseDTO buscarPorId(Integer id);
    List<AsignaturaResponseDTO> listarTodas();
    List<AsignaturaResponseDTO> listarPorCarrera(Integer idCarrera);
}