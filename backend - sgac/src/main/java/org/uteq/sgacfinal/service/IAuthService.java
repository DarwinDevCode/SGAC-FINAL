package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.*;


public interface IAuthService {
    UsuarioResponseDTO loginUsuario(LoginRequestDTO request);
}
