package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.Request.TipoEstadoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoPostulacionResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_estado_postulacion
 */
public interface ITipoEstadoPostulacionCatalogoService {

    StandardResponseDTO<List<TipoEstadoPostulacionResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(TipoEstadoPostulacionRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoPostulacionRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

