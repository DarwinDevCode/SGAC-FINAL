package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.UsuarioTipoRolRequestDTO;
import org.uteq.sgacfinal.dto.response.UsuarioTipoRolResponseDTO;
import java.util.List;

public interface IUsuarioTipoRolService {

    UsuarioTipoRolResponseDTO asignarRol(UsuarioTipoRolRequestDTO request);

    UsuarioTipoRolResponseDTO cambiarEstado(Integer idUsuario, Integer idRol, Boolean activo);

    void revocarRol(Integer idUsuario, Integer idRol);

    List<UsuarioTipoRolResponseDTO> listarRolesPorUsuario(Integer idUsuario);
}