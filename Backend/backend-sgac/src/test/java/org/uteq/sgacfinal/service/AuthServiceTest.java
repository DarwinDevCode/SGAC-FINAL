package org.uteq.sgacfinal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.uteq.sgacfinal.dto.Request.LoginRequestDTO;
import org.uteq.sgacfinal.repository.IAuthRepository;
import org.uteq.sgacfinal.service.impl.AuthServiceImpl;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private IAuthRepository authRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginUsuario_DebeLanzarExcepcion_CuandoCredencialesSonIncorrectas() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsuario("admin_falso");
        request.setPassword("clave_equivocada");

        when(authRepository.login("admin_falso", "clave_equivocada"))
                .thenReturn(new ArrayList<>());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.loginUsuario(request);
        });

        assertEquals("Credenciales incorrectas o usuario inactivo.", exception.getMessage());
    }
}
