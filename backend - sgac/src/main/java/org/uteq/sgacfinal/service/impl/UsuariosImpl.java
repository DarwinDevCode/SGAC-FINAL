package org.uteq.sgacfinal.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.entity.UsuarioTipoRol;
import org.uteq.sgacfinal.entity.UsuarioTipoRolId;
import org.uteq.sgacfinal.repository.IUsuarioTipoRolRepository;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.service.IUsuariosService;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.TipoRol;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuariosImpl implements IUsuariosService {
    private final IUsuariosRepository usuarioRepository;
    private final IUsuarioTipoRolRepository usuarioTipoRolRepository;
    @Override
    @Transactional
    public void registrarEstudiante(RegistroEstudianteRequestDTO dto) {
        usuarioRepository.registrarEstudiante(
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
        usuarioRepository.registrarDocente(
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
        usuarioRepository.registrarDecano(
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
        usuarioRepository.registrarCoordinador(
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
        usuarioRepository.registrarAdministrador(
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
        usuarioRepository.registrarAyudanteDirecto(
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
        usuarioRepository.promoverEstudianteAAyudante(
                dto.getUsername(),
                dto.getHorasAsignadas()
        );
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(u -> UsuarioResponseDTO.builder()
                        .idUsuario(u.getIdUsuario())
                        .nombres(u.getNombres())
                        .apellidos(u.getApellidos())
                        .correo(u.getCorreo())
                        .nombreUsuario(u.getNombreUsuario())
                        .activo(u.getActivo())
                        .token(null)

                        .roles(u.getRoles().stream()
                                .map(utr -> TipoRolResponseDTO.builder()
                                        .idTipoRol(utr.getTipoRol().getIdTipoRol())
                                        .nombreTipoRol(utr.getTipoRol().getNombreTipoRol())
                                        .activo(utr.getActivo())
                                        .build())
                                .toList())

                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void cambiarEstadoGlobal(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        usuario.setActivo(!usuario.getActivo());
        usuarioRepository.save(usuario);
    }

    @Override
    public void cambiarEstadoRol(Integer idUsuario, Integer idTipoRol) {
        UsuarioTipoRolId idCompuesto = new UsuarioTipoRolId(idUsuario, idTipoRol);
        UsuarioTipoRol relacion = usuarioTipoRolRepository.findById(idCompuesto)
                .orElseThrow(() -> new EntityNotFoundException("Rol no asignado a este usuario"));
        relacion.setActivo(!relacion.getActivo());
        usuarioTipoRolRepository.save(relacion);
    }
}
