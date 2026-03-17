package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.LoginRequestDTO;
import org.uteq.sgacfinal.dto.Request.SeleccionarRolRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;

public interface IAuthService {
    UsuarioResponseDTO loginUsuario(LoginRequestDTO request);
    UsuarioResponseDTO seleccionarRol(SeleccionarRolRequestDTO request);
}