package org.uteq.sgacfinal.controller.ayudantia;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.response.ayudantia.ParticipanteIdResponseDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.ParticipantePadronDTO;
import org.uteq.sgacfinal.service.IUsuarioSesionService;
import org.uteq.sgacfinal.service.ayudantia.ParticipanteService;

import java.util.List;

@RestController
@RequestMapping("/api/ayudantias/participantes")
@RequiredArgsConstructor
public class ParticipanteController {

    private final ParticipanteService participanteService;
    private final IUsuarioSesionService sesionService;

    @PostMapping("/gestionar")
    public ResponseEntity<RespuestaOperacionDTO<ParticipanteIdResponseDTO>> gestionarParticipante(
            @RequestBody ParticipanteRequestDTO request) {

        Integer idUsuario = sesionService.getIdUsuarioAutenticado();

        ParticipanteRequestDTO secureRequest = new ParticipanteRequestDTO(
                request.accion(),
                idUsuario,
                request.nombre(),
                request.curso(),
                request.paralelo(),
                request.idParticipante()
        );

        return ResponseEntity.ok(participanteService.gestionarParticipante(secureRequest));
    }

    @GetMapping
    public ResponseEntity<RespuestaOperacionDTO<List<ParticipantePadronDTO>>> listarPadron() {
        Integer idUsuario = sesionService.getIdUsuarioAutenticado();
        return ResponseEntity.ok(participanteService.listarPadron(idUsuario));
    }
}