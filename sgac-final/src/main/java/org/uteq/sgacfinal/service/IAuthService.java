package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.dto.UsuarioDTO;


public interface IAuthService {
    UsuarioDTO login(String usuario, String contrasenia);

    EstudianteResponseDTO registrarEstudiante(RegistroEstudianteRequestDTO request);
    DocenteResponseDTO registrarDocente(RegistroDocenteRequestDTO request);
    CoordinadorResponseDTO registrarCoordinador(RegistroCoordinadorRequestDTO request);
    DecanoResponseDTO  registrarDecano(RegistroDecanoRequestDTO request);
    AyudanteCatedraResponseDTO registrarAyudanteCatedra(RegistroAyudanteCatedraRequestDTO request);

    Integer RegistrarUsuario(UsuarioRequestDTO request);
}
