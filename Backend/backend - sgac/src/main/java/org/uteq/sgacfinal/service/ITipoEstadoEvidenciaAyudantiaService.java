package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoEstadoEvidenciaAyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaAyudantiaResponseDTO;
import java.util.List;

public interface ITipoEstadoEvidenciaAyudantiaService {
    List<TipoEstadoEvidenciaAyudantiaResponseDTO> listarTodos();
    TipoEstadoEvidenciaAyudantiaResponseDTO crear(TipoEstadoEvidenciaAyudantiaRequestDTO request);
    TipoEstadoEvidenciaAyudantiaResponseDTO actualizar(Integer id, TipoEstadoEvidenciaAyudantiaRequestDTO request);
    void desactivar(Integer id);
}