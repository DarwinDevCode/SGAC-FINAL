package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.RegistrarSesionRequest;
import org.uteq.sgacfinal.dto.Response.*;

import java.time.LocalDate;
import java.util.List;

public interface SesionService {
    List<SesionListadoResponse> listarSesiones(
            Integer idUsuario,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String estado,
            Integer idPeriodo
    );

    SesionDetalleResponse detalleSesion(Integer idUsuario, Integer idRegistro);
    List<EvidenciaResponse> evidenciasSesion(Integer idUsuario, Integer idRegistro);
    ProgresoGeneralResponse progresoGeneral(Integer idUsuario);
    ControlSemanalResponse controlSemanal(Integer idUsuario);
    RegistrarSesionResponse registrarSesion(Integer idUsuario, RegistrarSesionRequest request);
}