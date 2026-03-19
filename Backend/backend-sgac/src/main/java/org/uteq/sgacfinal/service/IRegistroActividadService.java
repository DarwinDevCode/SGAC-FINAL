package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.RegistroActividadRequestDTO;
import org.uteq.sgacfinal.dto.response.RegistroActividadResponseDTO;
import java.util.List;

public interface IRegistroActividadService {

    RegistroActividadResponseDTO crear(RegistroActividadRequestDTO request);

    RegistroActividadResponseDTO actualizar(Integer id, RegistroActividadRequestDTO request);

    void desactivar(Integer id);

    RegistroActividadResponseDTO buscarPorId(Integer id);

    List<RegistroActividadResponseDTO> listarPorAyudantia(Integer idAyudantia);
}