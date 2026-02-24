package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.dto.Request.FiltroPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Response.PermisoRolResponseDTO;

import java.util.List;

public interface IPermisoService {
    List<PermisoDTO> obtenerPermisos();

    List<PermisoRolResponseDTO> consultarPermisos(FiltroPermisosRequestDTO filtro);
}
