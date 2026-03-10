package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Response.NotificacionResponseDTO;

import java.util.List;

public interface INotificacionService {

    NotificacionResponseDTO enviarNotificacion(Integer idUsuario, NotificationRequest request);

    List<NotificacionResponseDTO> listarUltimas10DelUsuarioAutenticado();

    /**
     * Lista notificaciones NO leídas del usuario autenticado (orden desc por fecha).
     * @param limit si es null o <= 0, se devuelve todo.
     */
    List<NotificacionResponseDTO> listarNoLeidasDelUsuarioAutenticado(Integer limit);

    void marcarComoLeida(Integer idNotificacion);
}
