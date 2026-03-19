package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.request.TipoEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.TipoEvidenciaResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_evidencia
 */
public interface ITipoEvidenciaCatalogoService {

    StandardResponseDTO<List<TipoEvidenciaResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(TipoEvidenciaRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, TipoEvidenciaRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

