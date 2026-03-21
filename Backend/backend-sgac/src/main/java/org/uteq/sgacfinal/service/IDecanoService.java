package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.response.DecanoResponseDTO;
import org.uteq.sgacfinal.dto.response.DecanoEstadisticasDTO;
import org.uteq.sgacfinal.dto.response.ConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorPostulanteReporteDTO;
import org.uteq.sgacfinal.dto.response.LogAuditoriaDTO;
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

    List<CoordinadorPostulanteReporteDTO> reportePostulantesPorFacultad(Integer idFacultad);

    List<LogAuditoriaDTO> reporteAuditoriaPorFacultad(Integer idFacultad);
}