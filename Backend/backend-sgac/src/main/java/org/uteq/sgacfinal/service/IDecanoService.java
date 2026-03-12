package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.Response.DecanoResponseDTO;
import org.uteq.sgacfinal.dto.Response.DecanoEstadisticasDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaDTO;
import java.util.List;

public interface IDecanoService {

    DecanoResponseDTO crear(DecanoRequestDTO request);

    DecanoResponseDTO actualizar(Integer id, DecanoRequestDTO request);

    void desactivar(Integer id);

    DecanoResponseDTO buscarPorId(Integer id);

    DecanoResponseDTO buscarPorUsuario(Integer idUsuario);

    List<DecanoResponseDTO> listarActivos();

    DecanoEstadisticasDTO obtenerEstadisticasPorFacultad(Integer idFacultad);

    List<ConvocatoriaReporteDTO> reporteConvocatoriasPorFacultad(Integer idFacultad);

    List<LogAuditoriaDTO> reporteAuditoriaPorFacultad(Integer idFacultad);
}