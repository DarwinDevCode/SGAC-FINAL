package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.LoginResultadoDTO;
import org.uteq.sgacfinal.repository.IAuthRepository;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.service.IAuthService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {

    private final IAuthRepository authRepository;

    @Override
    public LoginResultadoDTO login(String usuario, String contrasenia) {

        List<Object[]> result = authRepository.login(usuario, contrasenia);

        if (result.isEmpty()) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        Object[] row = result.get(0);

        String rolesStr = (String) row[5];
        List<String> roles = rolesStr == null || rolesStr.isEmpty()
                ? List.of()
                : List.of(rolesStr.split(","));

        return LoginResultadoDTO.builder()
                .idUsuario((Integer) row[0])
                .nombres((String) row[1])
                .apellidos((String) row[2])
                .correo((String) row[3])
                .nombreUsuario((String) row[4])
                .roles(roles)
                .build();
    }

}
