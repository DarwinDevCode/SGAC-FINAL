package org.uteq.sgacfinal.service.impl.ayudantia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.response.ayudantia.ParticipanteIdResponseDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.ParticipantePadronDTO;
import org.uteq.sgacfinal.repository.ayudantia.ParticipanteRepository;
import org.uteq.sgacfinal.repository.ayudantia.PartipantePadronRepository;
import org.uteq.sgacfinal.service.ayudantia.ParticipanteService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipanteServiceImpl implements ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final PartipantePadronRepository partipantePadronRepository;

    @Override
    public RespuestaOperacionDTO<ParticipanteIdResponseDTO> gestionarParticipante(ParticipanteRequestDTO request) {
        log.info("[ParticipanteService] Gestionando participante: {} (Acción: {})", request.nombre(), request.accion());
        return participanteRepository.gestionarParticipante(request);
    }

    @Override
    public RespuestaOperacionDTO<List<ParticipantePadronDTO>> listarPadron(Integer idUsuario) {
        try {
            List<Object[]> resultados = partipantePadronRepository.listarPadronAyudante(idUsuario);

            List<ParticipantePadronDTO> padron = resultados.stream().map(row -> new ParticipantePadronDTO(
                    (Integer) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (Boolean) row[4]
            )).toList();

            return new RespuestaOperacionDTO<>(true, "Padrón recuperado exitosamente", padron);
        } catch (Exception e) {
            return new RespuestaOperacionDTO<>(false, "Error al obtener el padrón: " + e.getMessage(), null);
        }
    }
}