package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.CambiarEstadoActividadRequest;
import org.uteq.sgacfinal.dto.request.CambiarEstadoEvidenciaRequest;
import org.uteq.sgacfinal.dto.response.AyudanteResumenDTO;
import org.uteq.sgacfinal.dto.response.DocenteDashboardDTO;
import org.uteq.sgacfinal.dto.response.RegistroActividadDocenteDTO;

import java.util.List;

public interface IDocenteActividadesService {

    DocenteDashboardDTO getDashboard(Integer idUsuarioDocente);

    List<AyudanteResumenDTO> listarAyudantes(Integer idUsuarioDocente);

    List<RegistroActividadDocenteDTO> listarActividadesAyudante(Integer idAyudantia);

    RegistroActividadDocenteDTO getDetalleActividad(Integer idRegistroActividad);

    void cambiarEstadoActividad(Integer idRegistroActividad,
                                CambiarEstadoActividadRequest request,
                                Integer idUsuarioDocente);

    void cambiarEstadoEvidencia(Integer idEvidencia,
                                CambiarEstadoEvidenciaRequest request,
                                Integer idUsuarioDocente);
}
