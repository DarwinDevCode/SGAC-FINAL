package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.Request.TipoEstadoEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaResponseDTO;

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

