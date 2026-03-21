package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.EvidenciaRegistroActividadRequestDTO;
import org.uteq.sgacfinal.dto.response.EvidenciaRegistroActividadResponseDTO;
import java.util.List;

public interface IEvidenciaRegistroActividadService {

    EvidenciaRegistroActividadResponseDTO crear(EvidenciaRegistroActividadRequestDTO request);

    EvidenciaRegistroActividadResponseDTO actualizar(Integer id, EvidenciaRegistroActividadRequestDTO request);

    void desactivar(Integer id);

    EvidenciaRegistroActividadResponseDTO buscarPorId(Integer id);

    List<EvidenciaRegistroActividadResponseDTO> listarPorRegistroActividad(Integer idRegistroActividad);
}