package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.Request.TipoFaseRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoFaseResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_fase
 */
public interface ITipoFaseCatalogoService {

    StandardResponseDTO<List<TipoFaseResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(TipoFaseRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, TipoFaseRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

