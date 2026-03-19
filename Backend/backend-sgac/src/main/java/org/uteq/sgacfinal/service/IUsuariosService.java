package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.*;
import org.uteq.sgacfinal.dto.response.TipoRolResponseDTO;
import org.uteq.sgacfinal.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface IUsuariosService {
    void registrarUsuarioGlobal(RegistroUsuarioGlobalRequest dto);
    List<TipoRolResponseDTO> listarRolesActivos();
    void registrarEstudiante(RegistroEstudianteRequestDTO dto);
    void registrarDocente(RegistroDocenteRequestDTO dto);
    void registrarDecano(RegistroDecanoRequestDTO dto);
    void registrarCoordinador(RegistroCoordinadorRequestDTO dto);
    void registrarAdministrador(RegistroAdministradorRequest dto);
    void registrarAyudanteDirecto(RegistroAyudanteCatedraRequestDTO dto);
    void promoverEstudiante(PromoverEstudianteAyudanteRequest dto);
    List<UsuarioResponseDTO> listarTodos();
    void cambiarEstadoGlobal(Integer idUsuario);
    void cambiarEstadoRol(Integer idUsuario, Integer idTipoRol);
}