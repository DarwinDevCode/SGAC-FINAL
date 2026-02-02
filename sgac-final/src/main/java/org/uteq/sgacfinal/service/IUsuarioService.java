package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.UsuarioRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import java.util.List;

public interface IUsuarioService {

    UsuarioResponseDTO crear(UsuarioRequestDTO request);

    UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO request);

    void desactivar(Integer id);

    UsuarioResponseDTO buscarPorId(Integer id);

    UsuarioResponseDTO buscarPorNombreUsuario(String nombreUsuario);

    Object login(String usuario, String contrasenia);

    List<UsuarioResponseDTO> listarTodos();
}