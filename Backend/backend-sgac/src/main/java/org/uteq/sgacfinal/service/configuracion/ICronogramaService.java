package org.uteq.sgacfinal.service.configuracion;

import org.uteq.sgacfinal.dto.request.configuracion.AjusteCronogramaRequestDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.CronogramaActivoResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.PeriodoFaseResponseDTO;

import java.util.List;

public interface ICronogramaService {
    StandardResponseDTO<List<PeriodoFaseResponseDTO>> listarCronograma(Integer idPeriodo);
    StandardResponseDTO<Integer> guardarCronograma(AjusteCronogramaRequestDTO request);
    CronogramaActivoResponseDTO obtenerCronogramaActivo();
}
