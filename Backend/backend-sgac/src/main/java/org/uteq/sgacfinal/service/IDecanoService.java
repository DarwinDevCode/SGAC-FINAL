package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.Response.*;
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

    List<DecanoReporteCarreraDTO> reporteCarreras(Integer idFacultad);

    List<AsignaturaResponseDTO> reporteAsignaturas(Integer idFacultad);

    List<CoordinadorPostulanteReporteDTO> reportePostulantesFacultad(Integer idFacultad);

    List<DecanoReporteCoordinadorDTO> reporteCoordinadores(Integer idFacultad);
}