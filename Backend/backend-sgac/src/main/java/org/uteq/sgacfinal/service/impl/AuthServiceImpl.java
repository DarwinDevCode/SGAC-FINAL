package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.LoginRequestDTO;
import org.uteq.sgacfinal.dto.request.SeleccionarRolRequestDTO;
import org.uteq.sgacfinal.dto.response.TipoRolResponseDTO;
import org.uteq.sgacfinal.dto.response.UsuarioResponseDTO;
import org.uteq.sgacfinal.exception.AccesoDenegadoException;
import org.uteq.sgacfinal.repository.IAuthRepository;
import org.uteq.sgacfinal.security.JwtService;
import org.uteq.sgacfinal.service.IAuthService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {

    private final IAuthRepository authRepository;
    private final JwtService      jwtService;

    @Override
    public UsuarioResponseDTO loginUsuario(LoginRequestDTO request) {
        List<Object[]> resultados = authRepository.login(
                request.getUsuario(),
                request.getPassword()
        );

        if (resultados.isEmpty()) {
            throw new RuntimeException("Credenciales incorrectas o usuario inactivo.");
        }

        Object[] row = resultados.get(0);

        Integer idUsuario     = (Integer) row[0];
        String  nombres       = (String)  row[1];
        String  apellidos     = (String)  row[2];
        String  correo        = (String)  row[3];
        String  nombreUsuario = (String)  row[4];
        String  rolesStr      = (String)  row[5];

        List<TipoRolResponseDTO> listaRoles = new ArrayList<>();
        if (rolesStr != null && !rolesStr.isBlank()) {
            for (String rol : rolesStr.split(",")) {
                listaRoles.add(TipoRolResponseDTO.builder()
                        .nombreTipoRol(rol.trim())
                        .activo(true)
                        .build());
            }
        }

        String preAuthToken = jwtService.generatePreAuthToken(
                nombreUsuario,
                idUsuario,
                nombres,
                apellidos,
                correo,
                rolesStr != null ? rolesStr : ""
        );

        return UsuarioResponseDTO.builder()
                .idUsuario(idUsuario)
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(correo)
                .nombreUsuario(nombreUsuario)
                .rolActual(null)
                .roles(listaRoles)
                .token(preAuthToken)
                .activo(true)
                .build();
    }

    @Override
    public UsuarioResponseDTO seleccionarRol(SeleccionarRolRequestDTO request) {
        String preToken       = request.getPreAuthToken();
        String rolSolicitado  = request.getRolSeleccionado();

        if (!jwtService.isPreAuthTokenValid(preToken)) {
            throw new AccesoDenegadoException(
                    "El token de pre-autenticación es inválido, expirado o ha caducado. " +
                            "Inicia sesión nuevamente.");
        }

        String rolesCsv = jwtService.extractRolesCsv(preToken);
        List<String> rolesPermitidos = Arrays.asList(
                rolesCsv != null ? rolesCsv.split(",") : new String[0]
        );

        if (rolSolicitado == null || rolSolicitado.isBlank() ||
                !rolesPermitidos.contains(rolSolicitado.trim())) {
            throw new AccesoDenegadoException(
                    "El rol '" + rolSolicitado + "' no está asignado a este usuario.");
        }

        String  username  = jwtService.extractUsername(preToken);
        Integer idUsuario = jwtService.extractIdUsuario(preToken);
        String  nombres   = jwtService.extractNombres(preToken);
        String  apellidos = jwtService.extractApellidos(preToken);
        String  correo    = jwtService.extractCorreo(preToken);

        List<TipoRolResponseDTO> listaRoles = new ArrayList<>();
        for (String rol : rolesPermitidos) {
            if (!rol.isBlank()) {
                listaRoles.add(TipoRolResponseDTO.builder()
                        .nombreTipoRol(rol.trim())
                        .activo(true)
                        .build());
            }
        }

        String finalToken = jwtService.generateToken(username, rolSolicitado.trim(), idUsuario);

        return UsuarioResponseDTO.builder()
                .idUsuario(idUsuario)
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(correo)
                .nombreUsuario(username)
                .rolActual(rolSolicitado.trim())
                .roles(listaRoles)
                .token(finalToken)
                .activo(true)
                .build();
    }
}