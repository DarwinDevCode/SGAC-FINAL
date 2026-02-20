package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.EvaluacionMeritosRequestDTO;
import org.uteq.sgacfinal.dto.Response.EvaluacionMeritosResponseDTO;

public interface IEvaluacionMeritosService {

    EvaluacionMeritosResponseDTO crear(EvaluacionMeritosRequestDTO request);

    EvaluacionMeritosResponseDTO actualizar(Integer id, EvaluacionMeritosRequestDTO request);

    EvaluacionMeritosResponseDTO buscarPorId(Integer id);

    EvaluacionMeritosResponseDTO buscarPorPostulacion(Integer idPostulacion);
}