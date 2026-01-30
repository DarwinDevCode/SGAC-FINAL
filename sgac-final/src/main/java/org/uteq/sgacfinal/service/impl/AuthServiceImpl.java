package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.TipoRolDTO;
import org.uteq.sgacfinal.dto.UsuarioDTO;
import org.uteq.sgacfinal.repository.IAuthRepository;
import org.uteq.sgacfinal.service.IAuthService;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {
    private final IAuthRepository authRepository;

    @Override
    public UsuarioDTO login(String usuario, String contrasenia) {
        Supplier<List<Object[]>> loginSupplier = () ->
                authRepository.login(usuario, contrasenia);

        Consumer<List<Object[]>> validarCredenciales = result -> {
            if (result.isEmpty())
                throw new RuntimeException("Credenciales incorrectas");};

        Consumer<Object[]> validarUsuario = row -> {
            if (row[4] == null)
                throw new RuntimeException("Usuario inválido");
        };

        Consumer<Object[]> validarRoles = row -> {
            if (row[5] == null || row[5].toString().isBlank()) {
                throw new RuntimeException("Usuario sin roles asignados");
            }
        };

        Consumer<Object[]> validaciones =
                validarUsuario.andThen(validarRoles);

        List<Object[]> result = loginSupplier.get();
        validarCredenciales.accept(result);

        Object[] row = result.get(0);
        validaciones.accept(row);

        List<TipoRolDTO> roles = List.of(row[5].toString().split(","))
                .stream()
                .map(r -> TipoRolDTO.builder()
                        .nombreTipoRol(r)
                        .build())
                .toList();

        return UsuarioDTO.builder()
                .idUsuario((Integer) row[0])
                .nombres((String) row[1])
                .apellidos((String) row[2])
                .correo((String) row[3])
                .nombreUsuario((String) row[4])
                .roles(roles)
                .build();



















//        List<Object[]> result = authRepository.login(usuario, contrasenia);
//
//        if (result.isEmpty()) {
//            throw new RuntimeException("Credenciales incorrectas");
//        }
//
//        Object[] row = result.get(0);
//
//        List<TipoRolDTO> roles = row[5] == null
//                ? List.of()
//                : List.of(row[5].toString().split(","))
//                .stream()
//                .map(r -> TipoRolDTO.builder()
//                        .nombreTipoRol(r)
//                        .build())
//                .toList();
//
//        return UsuarioDTO.builder()
//                .idUsuario((Integer) row[0])
//                .nombres((String) row[1])
//                .apellidos((String) row[2])
//                .correo((String) row[3])
//                .nombreUsuario((String) row[4])
//                .roles(roles)
//                .build();
    }

}
