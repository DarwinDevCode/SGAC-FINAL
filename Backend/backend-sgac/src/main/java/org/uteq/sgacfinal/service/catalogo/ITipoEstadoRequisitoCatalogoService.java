package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.request.TipoEstadoRequisitoRequestDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.TipoEstadoRequisitoResponseDTO;

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

