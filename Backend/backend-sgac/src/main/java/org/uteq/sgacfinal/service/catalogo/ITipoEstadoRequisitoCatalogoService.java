package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.Request.TipoEstadoRequisitoRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRequisitoResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_estado_requisito
 */
public interface ITipoEstadoRequisitoCatalogoService {

    StandardResponseDTO<List<TipoEstadoRequisitoResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(TipoEstadoRequisitoRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoRequisitoRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

