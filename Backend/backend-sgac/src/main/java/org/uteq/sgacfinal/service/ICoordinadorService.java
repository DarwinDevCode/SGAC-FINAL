package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.CoordinadorRequestDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorResponseDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorEstadisticasDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorConvocatoriaReporteDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorPostulanteReporteDTO;
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