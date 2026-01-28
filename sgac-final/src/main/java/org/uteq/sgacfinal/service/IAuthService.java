package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.UsuarioDTO;

public interface IAuthService {
    UsuarioDTO login(String usuario, String contrasenia);
}
