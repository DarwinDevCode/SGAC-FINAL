package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.LoginRequestDTO;
import org.uteq.sgacfinal.dto.request.SeleccionarRolRequestDTO;
import org.uteq.sgacfinal.dto.response.UsuarioResponseDTO;

public interface IAuthService {
    UsuarioResponseDTO loginUsuario(LoginRequestDTO request);
    UsuarioResponseDTO seleccionarRol(SeleccionarRolRequestDTO request);
}