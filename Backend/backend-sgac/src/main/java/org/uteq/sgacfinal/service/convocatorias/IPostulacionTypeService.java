package org.uteq.sgacfinal.service.convocatorias;

import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.convocatorias.TribunalEvaluacionResponseDTO;

public interface IPostulacionTypeService {
    RespuestaOperacionDTO<TribunalEvaluacionResponseDTO> obtenerTribunalEvaluacion(Integer idUsuario);
}
