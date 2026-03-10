package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.Request.TipoSancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoSancionAyudanteCatedraResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre tipo_sancion_ayudante_catedra
 */
public interface ITipoSancionCatalogoService {

    /**
     * Lista todos los tipos de sanción activos
     */
    StandardResponseDTO<List<TipoSancionAyudanteCatedraResponseDTO>> listar();

    /**
     * Crea un nuevo tipo de sanción
     */
    StandardResponseDTO<Integer> crear(TipoSancionAyudanteCatedraRequestDTO request);

    /**
     * Actualiza un tipo de sanción existente
     */
    StandardResponseDTO<Integer> actualizar(Integer id, TipoSancionAyudanteCatedraRequestDTO request);

    /**
     * Desactiva (eliminación lógica) un tipo de sanción
     */
    StandardResponseDTO<Integer> desactivar(Integer id);
}

