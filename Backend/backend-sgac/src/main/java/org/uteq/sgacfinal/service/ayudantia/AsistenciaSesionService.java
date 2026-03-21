package org.uteq.sgacfinal.service.ayudantia;

import org.uteq.sgacfinal.dto.request.ayudantia.PlanificarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.AsistenciaSesionActualResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.MarcadoAsistenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.PlanificacionResponseDTO;

public interface AsistenciaSesionService {
    RespuestaOperacionDTO<PlanificacionResponseDTO> planificarSesion(PlanificarSesionRequestDTO request);
    RespuestaOperacionDTO<AsistenciaSesionActualResponseDTO> obtenerAsistenciaSesionActual(Integer idUsuario);
    RespuestaOperacionDTO<Void> marcarAsistencia(MarcadoAsistenciaRequestDTO request);
    RespuestaOperacionDTO<AsistenciaSesionActualResponseDTO> obtenerAsistenciaPorId(Integer idUsuario, Integer idRegistro);
}