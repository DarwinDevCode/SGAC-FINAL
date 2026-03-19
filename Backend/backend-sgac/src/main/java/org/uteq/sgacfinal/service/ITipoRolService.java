package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.TipoRolRequestDTO;
import org.uteq.sgacfinal.dto.response.RolResumenResponseDTO;
import org.uteq.sgacfinal.dto.response.TipoRolResponseDTO;
import java.util.List;

public interface ITipoRolService {
    List<TipoRolResponseDTO> listarTodos();
    List<TipoRolResponseDTO> listarActivos();
    TipoRolResponseDTO crear(TipoRolRequestDTO request);
    TipoRolResponseDTO actualizar(Integer id, TipoRolRequestDTO request);
    void desactivar(Integer id);
    List<RolResumenResponseDTO> obtenerRolesParaPermisos();
}