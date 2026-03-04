package org.uteq.sgacfinal.service;

/**
 * Utilidad de sesión para obtener datos del usuario autenticado.
 */
public interface IUsuarioSesionService {

    /** @return id_usuario del usuario autenticado */
    Integer getIdUsuarioAutenticado();
}

