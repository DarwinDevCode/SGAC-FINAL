package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.*;

public interface IRegistroUsuariosService {
    void registrarEstudiante(RegistroEstudianteRequestDTO dto);
    void registrarDocente(RegistroDocenteRequestDTO dto);
    void registrarDecano(RegistroDecanoRequestDTO dto);
    void registrarCoordinador(RegistroCoordinadorRequestDTO dto);
    void registrarAdministrador(RegistroAdministradorRequest dto);
    void registrarAyudanteDirecto(RegistroAyudanteCatedraRequestDTO dto);
    void promoverEstudiante(PromoverEstudianteAyudanteRequest dto);
}
