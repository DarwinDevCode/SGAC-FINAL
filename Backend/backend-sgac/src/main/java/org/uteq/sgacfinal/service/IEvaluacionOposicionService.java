package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.AsignarComisionRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.response.EvaluacionOposicionResponseDTO;
import java.util.List;

public interface IEvaluacionOposicionService {

    EvaluacionOposicionResponseDTO crear(EvaluacionOposicionRequestDTO request);

    EvaluacionOposicionResponseDTO actualizar(Integer id, EvaluacionOposicionRequestDTO request);

    EvaluacionOposicionResponseDTO buscarPorId(Integer id);

    EvaluacionOposicionResponseDTO buscarPorPostulacion(Integer idPostulacion);

    List<EvaluacionOposicionResponseDTO> listarTodas();

    EvaluacionOposicionResponseDTO asignarComisionAPostulacion(AsignarComisionRequestDTO request);
}