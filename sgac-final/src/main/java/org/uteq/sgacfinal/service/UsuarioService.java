package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.UsuarioDTO;
import org.uteq.sgacfinal.dto.UsuarioRequest;

import java.util.List;

public interface UsuarioService {

    List<UsuarioDTO> findAll();

    List<UsuarioDTO> findAllActive();

    UsuarioDTO findById(Integer id);

    UsuarioDTO findByNombreUsuario(String nombreUsuario);

    UsuarioDTO create(UsuarioRequest request);

    UsuarioDTO update(Integer id, UsuarioRequest request);

    void delete(Integer id);

    void toggleActive(Integer id);

    void assignRole(Integer usuarioId, Integer tipoRolId);

    void removeRole(Integer usuarioId, Integer tipoRolId);
}
