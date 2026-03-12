package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import java.util.List;

public interface IPdfGeneratorService {
    byte[] generarReporteAuditoria(List<LogAuditoriaResponseDTO> logs, String filtros);
}
