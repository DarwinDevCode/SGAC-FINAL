package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoEstadoEvidenciaAyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaAyudantiaResponseDTO;
import java.util.List;

public interface ITipoEstadoEvidenciaAyudantiaService {

    TipoEstadoEvidenciaAyudantiaResponseDTO crear(TipoEstadoEvidenciaAyudantiaRequestDTO request);

    TipoEstadoEvidenciaAyudantiaResponseDTO actualizar(Integer id, TipoEstadoEvidenciaAyudantiaRequestDTO request);

    void eliminar(Integer id);

    TipoEstadoEvidenciaAyudantiaResponseDTO buscarPorId(Integer id);

    List<TipoEstadoEvidenciaAyudantiaResponseDTO> listarTodos();
}