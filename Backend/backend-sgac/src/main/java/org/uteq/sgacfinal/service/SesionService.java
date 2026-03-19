package org.uteq.sgacfinal.service;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.RegistrarSesionRequest;
import org.uteq.sgacfinal.dto.response.*;

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
    RegistrarSesionResponse registrarSesion(Integer idUsuario, RegistrarSesionRequest request, List<MultipartFile> archivos);

}