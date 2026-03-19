package org.uteq.sgacfinal.service.impl.ayudantia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteIdResponseDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.repository.ayudantia.ParticipanteRepository;
import org.uteq.sgacfinal.service.ayudantia.ParticipanteService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipanteServiceImpl implements ParticipanteService {

    private final ParticipanteRepository participanteRepository;

    @Override
    public RespuestaOperacionDTO<ParticipanteIdResponseDTO> gestionarParticipante(ParticipanteRequestDTO request) {
        log.info("[ParticipanteService] Gestionando participante: {} (Acción: {})", request.nombre(), request.accion());
        return participanteRepository.gestionarParticipante(request);
    }
}