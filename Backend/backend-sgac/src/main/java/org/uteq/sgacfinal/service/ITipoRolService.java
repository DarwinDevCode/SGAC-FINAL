package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.TipoRolRequestDTO;
import org.uteq.sgacfinal.dto.Response.RolResumenResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import java.util.List;

public interface ITipoRolService {
    List<TipoRolResponseDTO> listarTodos();
    List<TipoRolResponseDTO> listarActivos();
    TipoRolResponseDTO crear(TipoRolRequestDTO request);
    TipoRolResponseDTO actualizar(Integer id, TipoRolRequestDTO request);
    void desactivar(Integer id);
    List<RolResumenResponseDTO> obtenerRolesParaPermisos();
}