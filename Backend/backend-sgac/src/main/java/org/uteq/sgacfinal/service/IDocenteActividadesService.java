package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.CambiarEstadoActividadRequest;
import org.uteq.sgacfinal.dto.Request.CambiarEstadoEvidenciaRequest;
import org.uteq.sgacfinal.dto.Response.AyudanteResumenDTO;
import org.uteq.sgacfinal.dto.Response.DocenteDashboardDTO;
import org.uteq.sgacfinal.dto.Response.RegistroActividadDocenteDTO;

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
