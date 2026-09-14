package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.IaAsistenteRequestDTO;
import org.uteq.sgacfinal.dto.response.IaAsistenteResponseDTO;

public interface IIaAsistenteService {
    IaAsistenteResponseDTO consultar(IaAsistenteRequestDTO request, Integer idUsuario);
}
