package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.repository.IRegistroUsuariosRepository;
import org.uteq.sgacfinal.service.IRegistroUsuariosService;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistroUsuariosImpl implements IRegistroUsuariosService {
    private final IRegistroUsuariosRepository registroUsuariosRepository;

    @Override
    @Transactional
    public void registrarEstudiante(RegistroEstudianteRequestDTO dto) {
        registroUsuariosRepository.registrarEstudiante(
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCedula(),
                dto.getCorreo(),
                dto.getUsername(),
                dto.getPassword(),
                dto.getIdCarrera(),
                dto.getMatricula(),
                dto.getSemestre());
    }

    @Override
    @Transactional
    public void registrarDocente(RegistroDocenteRequestDTO dto) {
        registroUsuariosRepository.registrarDocente(
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCedula(),
                dto.getCorreo(),
                dto.getUsername(),
                dto.getPassword()
        );
    }

    @Override
    @Transactional
    public void registrarDecano(RegistroDecanoRequestDTO dto) {
        registroUsuariosRepository.registrarDecano(
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCedula(),
                dto.getCorreo(),
                dto.getUsername(),
                dto.getPassword(),
                dto.getIdFacultad()
        );
    }

    @Override
    @Transactional
    public void registrarCoordinador(RegistroCoordinadorRequestDTO dto) {
        registroUsuariosRepository.registrarCoordinador(
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCedula(),
                dto.getCorreo(),
                dto.getUsername(),
                dto.getPassword(),
                dto.getIdCarrera()
        );
    }

    @Override
    @Transactional
    public void registrarAdministrador(RegistroAdministradorRequest dto) {
        registroUsuariosRepository.registrarAdministrador(
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCedula(),
                dto.getCorreo(),
                dto.getUsername(),
                dto.getPassword()
        );
    }

    @Override
    @Transactional
    public void registrarAyudanteDirecto(RegistroAyudanteCatedraRequestDTO dto) {
        registroUsuariosRepository.registrarAyudanteDirecto(
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCedula(),
                dto.getCorreo(),
                dto.getUsername(),
                dto.getPassword(),
                dto.getHorasAyudante()
        );
    }

    @Override
    @Transactional
    public void promoverEstudiante(PromoverEstudianteAyudanteRequest dto) {
        registroUsuariosRepository.promoverEstudianteAAyudante(
                dto.getUsername(),
                dto.getHorasAsignadas()
        );
    }
}
