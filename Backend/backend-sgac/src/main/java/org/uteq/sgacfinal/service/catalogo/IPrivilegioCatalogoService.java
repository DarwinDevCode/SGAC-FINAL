package org.uteq.sgacfinal.service.catalogo;

import org.uteq.sgacfinal.dto.request.PrivilegioRequestDTO;
import org.uteq.sgacfinal.dto.response.PrivilegioFuncionResponseDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para operaciones CRUD sobre privilegio
 */
public interface IPrivilegioCatalogoService {

    StandardResponseDTO<List<PrivilegioFuncionResponseDTO>> listar();

    StandardResponseDTO<Integer> crear(PrivilegioRequestDTO request);

    StandardResponseDTO<Integer> actualizar(Integer id, PrivilegioRequestDTO request);

    StandardResponseDTO<Integer> desactivar(Integer id);
}

