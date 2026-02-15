package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.FacultadRequestDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import java.util.List;

public interface IFacultadService {

    FacultadResponseDTO crear(FacultadRequestDTO request);

    FacultadResponseDTO actualizar(Integer id, FacultadRequestDTO request);

    void desactivar(Integer id);

    FacultadResponseDTO buscarPorId(Integer id);

    List<FacultadResponseDTO> listarTodas();
}