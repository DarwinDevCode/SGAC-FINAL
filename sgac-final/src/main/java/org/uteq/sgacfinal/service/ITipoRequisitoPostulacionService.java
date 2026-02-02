package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRequisitoPostulacionResponseDTO;
import java.util.List;

public interface ITipoRequisitoPostulacionService {

    TipoRequisitoPostulacionResponseDTO crear(TipoRequisitoPostulacionRequestDTO request);

    TipoRequisitoPostulacionResponseDTO actualizar(Integer id, TipoRequisitoPostulacionRequestDTO request);

    void eliminar(Integer id);

    TipoRequisitoPostulacionResponseDTO buscarPorId(Integer id);

    List<TipoRequisitoPostulacionResponseDTO> listarTodos();
}