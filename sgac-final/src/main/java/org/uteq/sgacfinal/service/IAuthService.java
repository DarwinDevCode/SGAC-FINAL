package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.LoginResultadoDTO;

public interface IAuthService {
    LoginResultadoDTO login(String usuario, String contrasenia);
}
