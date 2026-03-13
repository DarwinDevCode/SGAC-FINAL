package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Response.AdminConsultaDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;

import java.util.List;

public interface IExcelGeneratorService {
    byte[] generarExcelUsuarios(List<UsuarioResponseDTO> usuarios);
    byte[] generarExcelCatalogos(List<FacultadResponseDTO> facultades, List<CarreraResponseDTO> carreras);
    byte[] generarMatrizPermisos();
    byte[] generarExcelDashboard(AdminConsultaDTO dashboardData);
    byte[] generarExcelAuditoria(List<org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO> logs);
}
