package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;

import java.util.List;

public interface IUsuariosService {
    void registrarEstudiante(RegistroEstudianteRequestDTO dto);
    void registrarDocente(RegistroDocenteRequestDTO dto);
    void registrarDecano(RegistroDecanoRequestDTO dto);
    void registrarCoordinador(RegistroCoordinadorRequestDTO dto);
    void registrarAdministrador(RegistroAdministradorRequest dto);
    void registrarAyudanteDirecto(RegistroAyudanteCatedraRequestDTO dto);
    void promoverEstudiante(PromoverEstudianteAyudanteRequest dto);
    public List<UsuarioResponseDTO> listarTodos();
    void cambiarEstadoGlobal(Integer idUsuario);
    void cambiarEstadoRol(Integer idUsuario, Integer idTipoRol);
}
