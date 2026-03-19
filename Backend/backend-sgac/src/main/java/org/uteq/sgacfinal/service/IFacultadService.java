package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.FacultadRequestDTO;
import org.uteq.sgacfinal.dto.response.FacultadResponseDTO;
import java.util.List;

public interface IFacultadService {

    FacultadResponseDTO crear(FacultadRequestDTO request);

    FacultadResponseDTO actualizar(Integer id, FacultadRequestDTO request);

    void desactivar(Integer id);

    FacultadResponseDTO buscarPorId(Integer id);

    List<FacultadResponseDTO> listarTodas();
}