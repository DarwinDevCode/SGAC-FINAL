package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.Request.TipoEstadoAyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoAyudantiaResponseDTO;

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

