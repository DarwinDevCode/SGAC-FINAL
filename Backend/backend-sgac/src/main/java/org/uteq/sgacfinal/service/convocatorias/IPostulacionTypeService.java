package org.uteq.sgacfinal.service.convocatorias;

import org.uteq.sgacfinal.dto.Response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.Response.convocatorias.TribunalEvaluacionResponseDTO;

public interface IPostulacionTypeService {
    RespuestaOperacionDTO<TribunalEvaluacionResponseDTO> obtenerTribunalEvaluacion(Integer idUsuario);
}
