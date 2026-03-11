package org.uteq.sgacfinal.service.configuracion;

import org.uteq.sgacfinal.dto.Request.configuracion.AjusteCronogramaRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.CronogramaActivoResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.PeriodoFaseResponseDTO;

import java.util.List;

public interface ICronogramaService {
    StandardResponseDTO<List<PeriodoFaseResponseDTO>> listarCronograma(Integer idPeriodo);
    StandardResponseDTO<Integer> guardarCronograma(AjusteCronogramaRequestDTO request);
    CronogramaActivoResponseDTO obtenerCronogramaActivo();
}
