package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.repository.IAuthRepository;
import org.uteq.sgacfinal.service.IAuthService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {

    private final IAuthRepository authRepository;

    @Override
    public UsuarioResponseDTO loginUsuario(LoginRequestDTO request) {
        // fn_login_sgac corre como SECURITY DEFINER → tiene acceso al schema seguridad
        // No hacemos ninguna query JPA adicional porque app_user_default no tiene
        // permisos directos sobre seguridad.*
        List<Object[]> resultados = authRepository.login(
                request.getUsuario(),
                request.getPassword()
        );

        if (resultados.isEmpty())
            throw new RuntimeException("Credenciales incorrectas o usuario inactivo.");

        Object[] row = resultados.get(0);

        // fn_login_sgac retorna: id_usuario, nombres, apellidos, correo, nombre_usuario, roles (csv)
        Integer idUsuario   = (Integer) row[0];
        String  nombres     = (String)  row[1];
        String  apellidos   = (String)  row[2];
        String  correo      = (String)  row[3];
        String  nombreUsuario = (String) row[4];
        String  rolesStr    = (String)  row[5]; // ej: "ESTUDIANTE,AYUDANTE_CATEDRA"

        // Construir lista de roles desde el string CSV que devuelve la función
        List<TipoRolResponseDTO> listaRoles = new ArrayList<>();
        if (rolesStr != null && !rolesStr.isBlank()) {
            for (String rol : rolesStr.split(",")) {
                listaRoles.add(TipoRolResponseDTO.builder()
                        .nombreTipoRol(rol.trim())
                        .activo(true)
                        .build());
            }
        }

        String rolPrincipal = listaRoles.isEmpty()
                ? "SIN_ROL"
                : listaRoles.get(0).getNombreTipoRol();

        return UsuarioResponseDTO.builder()
                .idUsuario(idUsuario)
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(correo)
                .nombreUsuario(nombreUsuario)
                .rolActual(rolPrincipal)
                .roles(listaRoles)
                .activo(true)
                .build();
    }
}
