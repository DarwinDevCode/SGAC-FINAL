package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.request.TipoEstadoAyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.TipoEstadoAyudantiaResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_estado_ayudantia
 */
public interface ITipoEstadoAyudantiaCatalogoService {

    StandardResponseDTO<List<TipoEstadoAyudantiaResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(TipoEstadoAyudantiaRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoAyudantiaRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

