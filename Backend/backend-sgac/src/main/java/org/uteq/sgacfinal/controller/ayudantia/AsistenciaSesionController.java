package org.uteq.sgacfinal.controller.ayudantia;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.request.ayudantia.PlanificarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.AsistenciaSesionActualResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.MarcadoAsistenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.PlanificacionResponseDTO;
import org.uteq.sgacfinal.service.IUsuarioSesionService;
import org.uteq.sgacfinal.service.ayudantia.AsistenciaSesionService;

@RestController
@RequestMapping("/api/ayudantias/sesiones")
@RequiredArgsConstructor
public class AsistenciaSesionController {

    private final AsistenciaSesionService asistenciaService;
    private final IUsuarioSesionService sesionService;

    @PostMapping("/planificar")
    public ResponseEntity<RespuestaOperacionDTO<PlanificacionResponseDTO>> planificarSesion(
            @RequestBody PlanificarSesionRequestDTO request) {

        Integer idUsuario = sesionService.getIdUsuarioAutenticado();

        PlanificarSesionRequestDTO secureRequest = new PlanificarSesionRequestDTO(
                idUsuario,
                request.fecha(),
                request.horaInicio(),
                request.horaFin(),
                request.lugar(),
                request.tema()
        );

        return ResponseEntity.ok(asistenciaService.planificarSesion(secureRequest));
    }

    @GetMapping("/actual")
    public ResponseEntity<RespuestaOperacionDTO<AsistenciaSesionActualResponseDTO>> obtenerSesionActual() {
        Integer idUsuario = sesionService.getIdUsuarioAutenticado();
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciaSesionActual(idUsuario));
    }

    @PatchMapping("/marcar-asistencia")
    public ResponseEntity<RespuestaOperacionDTO<Void>> marcarAsistencia(
            @RequestBody MarcadoAsistenciaRequestDTO request) {
        return ResponseEntity.ok(asistenciaService.marcarAsistencia(request));
    }
}