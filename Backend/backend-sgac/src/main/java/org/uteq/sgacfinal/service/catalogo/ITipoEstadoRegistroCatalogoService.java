package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.Request.TipoEstadoRegistroRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRegistroResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_estado_registro
 */
public interface ITipoEstadoRegistroCatalogoService {

    StandardResponseDTO<List<TipoEstadoRegistroResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(TipoEstadoRegistroRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoRegistroRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

