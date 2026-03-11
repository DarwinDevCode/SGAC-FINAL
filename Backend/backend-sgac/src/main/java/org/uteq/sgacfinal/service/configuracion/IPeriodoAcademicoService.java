package org.uteq.sgacfinal.service.configuracion;

import org.uteq.sgacfinal.dto.Request.configuracion.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;

public interface IPeriodoAcademicoService {
    StandardResponseDTO<Integer> abrirPeriodo(PeriodoAcademicoRequestDTO request);
    StandardResponseDTO<Integer> iniciarPeriodo(Integer idPeriodo);
}
