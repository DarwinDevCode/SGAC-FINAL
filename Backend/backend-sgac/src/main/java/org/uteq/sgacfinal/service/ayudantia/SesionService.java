package org.uteq.sgacfinal.service.ayudantia;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.ayudantia.CompletarSesionRequest;
import org.uteq.sgacfinal.dto.Request.ayudantia.EvaluarSesionRequest;
import org.uteq.sgacfinal.dto.Request.ayudantia.PlanificarSesionRequest;
import org.uteq.sgacfinal.dto.Response.SesionDTO;
import org.uteq.sgacfinal.dto.Response.ayudantia.CompletarSesionResponse;
import org.uteq.sgacfinal.dto.Response.ayudantia.EvaluarSesionResponse;
import org.uteq.sgacfinal.dto.Response.ayudantia.PlanificarSesionResponse;

import java.time.LocalDate;
import java.util.List;

public interface SesionService {
    PlanificarSesionResponse planificarSesion(
            Integer idUsuarioAyudante,
            PlanificarSesionRequest request);

    CompletarSesionResponse completarSesion(
            Integer idUsuarioAyudante,
            Integer idRegistroActividad,
            CompletarSesionRequest request,
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