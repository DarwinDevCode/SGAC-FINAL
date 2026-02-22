package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.IAuthService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {
    private final IAuthRepository authRepository;
    private final IUsuariosRepository usuarioRepository;

    @Override
    @Transactional
    public UsuarioResponseDTO loginUsuario(LoginRequestDTO request) {
        List<Object[]> resultados = authRepository.login(
                request.getUsuario(),
                request.getPassword()
        );

        if (resultados.isEmpty())
            throw new RuntimeException("Credenciales incorrectas o usuario inactivo.");

        Object[] row = resultados.get(0);
        Integer idUsuario = (Integer) row[0];

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Error de integridad: Usuario no encontrado."));

        List<TipoRolResponseDTO> listaRoles = usuario.getRoles().stream()
                .map(utr -> TipoRolResponseDTO.builder()
                        .idTipoRol(utr.getTipoRol().getIdTipoRol())
                        .nombreTipoRol(utr.getTipoRol().getNombreTipoRol())
                        .activo(utr.getActivo())
                        .build())
                .toList();

        String rolPrincipal = listaRoles.stream()
                .filter(TipoRolResponseDTO::getActivo)
                .map(TipoRolResponseDTO::getNombreTipoRol)
                .findFirst()
                .orElse("SIN_ROL");

        return UsuarioResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .nombreUsuario(usuario.getNombreUsuario())
                .rolActual(rolPrincipal)
                .roles(listaRoles)
                .activo(usuario.getActivo())
                .build();

    }
}
