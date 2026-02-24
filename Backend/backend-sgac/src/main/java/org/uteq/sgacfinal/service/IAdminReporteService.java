package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.response.AdminReporteGlobalDTO;
import org.uteq.sgacfinal.dto.response.AuditoriaGlobalDTO;

import java.util.List;

public interface IAdminReporteService {
    List<AdminReporteGlobalDTO.UsuarioDTO> reporteGlobalUsuarios();
    List<AdminReporteGlobalDTO.PersonalDTO> reporteGlobalPersonal();
    List<AdminReporteGlobalDTO.PostulanteDTO> reporteGlobalPostulantes();
    List<AdminReporteGlobalDTO.AyudanteDTO> reporteGlobalAyudantes();
    List<AuditoriaGlobalDTO> reporteAuditoriaGlobal();
}
