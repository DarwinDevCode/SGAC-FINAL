package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.CarreraRequestDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import java.util.List;

public interface ICarreraService {

    CarreraResponseDTO crear(CarreraRequestDTO request);

    CarreraResponseDTO actualizar(Integer id, CarreraRequestDTO request);

    void desactivar(Integer id);

    CarreraResponseDTO buscarPorId(Integer id);

    List<CarreraResponseDTO> listarTodas();

    List<CarreraResponseDTO> listarPorFacultad(Integer idFacultad);
}