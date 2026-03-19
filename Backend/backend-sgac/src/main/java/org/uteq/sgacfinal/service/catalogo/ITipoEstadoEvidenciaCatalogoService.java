package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.request.TipoEstadoEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.TipoEstadoEvidenciaResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_estado_evidencia
 */
public interface ITipoEstadoEvidenciaCatalogoService {

    StandardResponseDTO<List<TipoEstadoEvidenciaResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(TipoEstadoEvidenciaRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoEvidenciaRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

