package org.uteq.sgacfinal.service.ayudantia;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.CompletarSesionRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarSesionRequest;
import org.uteq.sgacfinal.dto.request.PlanificarSesionRequest;
import org.uteq.sgacfinal.dto.response.SesionDTO;
import org.uteq.sgacfinal.dto.response.CompletarSesionResponseDTO;
import org.uteq.sgacfinal.dto.response.EvaluarSesionResponse;
import org.uteq.sgacfinal.dto.response.PlanificarSesionResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface SesionService {
    PlanificarSesionResponseDTO planificarSesion(
            Integer idUsuarioAyudante,
            PlanificarSesionRequest request);

    CompletarSesionResponseDTO completarSesion(
            Integer idUsuarioAyudante,
            Integer idRegistroActividad,
            CompletarSesionRequestDTO request,
            List<MultipartFile> archivos);

    EvaluarSesionResponse evaluarSesion(
            Integer idUsuarioDocente,
            Integer idRegistroActividad,
            EvaluarSesionRequest request);

    List<SesionDTO> listarSesiones(
            Integer   idUsuario,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String    estadoCodigo,
            Integer   idAyudantia);

    SesionDTO detalleSesion(Integer idUsuario, Integer idRegistroActividad);
}