package org.uteq.sgacfinal.service.reportes_y_auditoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaKpiDTO;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaResponseDTO;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.EvolucionAuditoriaProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaService {
    Page<AuditoriaResponseDTO> buscarAuditorias(
            String tabla,
            String accion,
            Integer idUsuario,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable
    );

    List<EvolucionAuditoriaProjection> obtenerEvolucion();
    AuditoriaKpiDTO obtenerKpis();
}