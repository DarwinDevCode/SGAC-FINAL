package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Response.EvaluacionOposicionResponseDTO;
import java.util.List;

public interface IEvaluacionOposicionService {

    EvaluacionOposicionResponseDTO crear(EvaluacionOposicionRequestDTO request);

    EvaluacionOposicionResponseDTO actualizar(Integer id, EvaluacionOposicionRequestDTO request);

    EvaluacionOposicionResponseDTO buscarPorId(Integer id);

    EvaluacionOposicionResponseDTO buscarPorPostulacion(Integer idPostulacion);

    List<EvaluacionOposicionResponseDTO> listarTodas();
}