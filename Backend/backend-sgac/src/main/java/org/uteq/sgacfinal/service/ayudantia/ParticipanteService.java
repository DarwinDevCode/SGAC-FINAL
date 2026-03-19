package org.uteq.sgacfinal.service.ayudantia;

import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteIdResponseDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;

public interface ParticipanteService {
    RespuestaOperacionDTO<ParticipanteIdResponseDTO> gestionarParticipante(ParticipanteRequestDTO request);
}