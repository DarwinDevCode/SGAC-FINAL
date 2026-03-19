package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.CoordinadorRequestDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorResponseDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorEstadisticasDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.response.CoordinadorPostulanteReporteDTO;
import java.util.List;

public interface ICoordinadorService {

    CoordinadorResponseDTO crear(CoordinadorRequestDTO request);

    CoordinadorResponseDTO actualizar(Integer id, CoordinadorRequestDTO request);

    void desactivar(Integer id);

    CoordinadorResponseDTO buscarPorId(Integer id);

    CoordinadorResponseDTO buscarPorUsuario(Integer idUsuario);

    List<CoordinadorResponseDTO> listarTodos();

    CoordinadorEstadisticasDTO obtenerEstadisticasPropias(Integer idUsuario);

    List<CoordinadorConvocatoriaReporteDTO> reporteConvocatoriasPropias(Integer idUsuario);

    List<CoordinadorPostulanteReporteDTO> reportePostulantesPropios(Integer idUsuario);
}