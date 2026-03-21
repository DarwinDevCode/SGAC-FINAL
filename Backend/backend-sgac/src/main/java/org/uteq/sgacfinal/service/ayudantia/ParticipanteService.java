package org.uteq.sgacfinal.service.ayudantia;

import org.uteq.sgacfinal.dto.response.ayudantia.ParticipanteIdResponseDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.ParticipantePadronDTO;

import java.util.List;

public interface ParticipanteService {
    RespuestaOperacionDTO<ParticipanteIdResponseDTO> gestionarParticipante(ParticipanteRequestDTO request);
    RespuestaOperacionDTO<List<ParticipantePadronDTO>> listarPadron(Integer idUsuario);
}