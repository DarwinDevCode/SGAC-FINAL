package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Response.*;
import java.util.List;

public interface IAdminReporteService {
    
    List<LogAuditoriaResponseDTO> reporteAuditoriaCompleto(Integer idUsuario, String modulo, String fechaDesde, String fechaHasta);
    
    List<AdminReporteGeneralDTO> reporteFacultades();
    
    List<AdminReporteGeneralDTO> reporteCarreras(Integer idFacultad);
    
    List<AdminReporteGeneralDTO> reporteAsignaturas(Integer idCarrera);
    
    List<AdminReporteGeneralDTO> reporteConvocatorias(Integer idAsignatura, Integer idPeriodo);
    
    List<AdminReporteGeneralDTO> reporteDecanosCoordinadores(Integer idFacultad, Integer idCarrera, String tipo);
    
    List<CoordinadorPostulanteReporteDTO> reportePostulantesGlobal(Integer idAsignatura, Integer idPeriodo, String estado);
    
    List<UsuarioReporteDTO> reporteUsuarios(String rol, Boolean activo);
}
