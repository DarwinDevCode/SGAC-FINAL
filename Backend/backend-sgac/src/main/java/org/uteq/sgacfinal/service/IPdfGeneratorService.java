package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import java.util.List;

public interface IPdfGeneratorService {
    byte[] generarReporteAuditoria(List<LogAuditoriaResponseDTO> logs, String filtros);
    byte[] generarReporteUsuarios(List<UsuarioResponseDTO> usuarios);
    byte[] generarReporteCatalogos(List<FacultadResponseDTO> facultades, List<CarreraResponseDTO> carreras);
    byte[] generarMatrizPermisos(String permisosDataStr); // will pass JSON str or mapped DTOs
    byte[] generarReporteDashboard(String dashboardDataStr); // will pass basic stats
}
