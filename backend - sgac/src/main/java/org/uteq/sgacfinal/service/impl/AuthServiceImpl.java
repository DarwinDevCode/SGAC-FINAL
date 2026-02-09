package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.IAuthService;
import org.uteq.sgacfinal.service.IEstudianteService;
import org.uteq.sgacfinal.service.IUsuarioService;
import org.uteq.sgacfinal.service.IUsuarioTipoRolService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {
    private final IAuthRepository authRepository;

    @Override
    public UsuarioResponseDTO loginUsuario(LoginRequestDTO request) {
        List<Object[]> resultados = authRepository.login(
                request.getUsuario(),
                request.getPassword()
        );

        if (resultados.isEmpty())
            throw new RuntimeException("Credenciales incorrectas o usuario inactivo.");

        Object[] row = resultados.get(0);

        String rolesConcatenados = (String) row[5];

        if (rolesConcatenados == null || rolesConcatenados.isBlank())
            throw new RuntimeException("El usuario no tiene roles asignados.");

        String[] arrayRoles = rolesConcatenados.split(",");

        String rolPrincipal = arrayRoles[0];

        List<String> listaRoles = List.of(arrayRoles);

        return UsuarioResponseDTO.builder()
                .idUsuario((Integer) row[0])
                .nombres((String) row[1])
                .apellidos((String) row[2])
                .correo((String) row[3])
                .nombreUsuario((String) row[4])
                .rolActual(rolPrincipal)
                .roles(listaRoles)
                .build();
    }
}
