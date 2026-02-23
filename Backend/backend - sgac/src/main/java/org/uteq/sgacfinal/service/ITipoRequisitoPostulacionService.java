package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRequisitoPostulacionResponseDTO;
import java.util.List;

public interface ITipoRequisitoPostulacionService {
    List<TipoRequisitoPostulacionResponseDTO> listarTodos();
    List<TipoRequisitoPostulacionResponseDTO> listarActivos();
    TipoRequisitoPostulacionResponseDTO crear(TipoRequisitoPostulacionRequestDTO request);
    TipoRequisitoPostulacionResponseDTO actualizar(Integer id, TipoRequisitoPostulacionRequestDTO request);
    void desactivar(Integer id);
    void eliminar(Integer id);
    TipoRequisitoPostulacionResponseDTO buscarPorId(Integer id);
    List<TipoRequisitoPostulacionResponseDTO> listarRequisitosActivos();
}