package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Response.NotificacionResponseDTO;

import java.util.List;

public interface INotificacionService {

    NotificacionResponseDTO enviarNotificacion(Long idUsuario, NotificationRequest request);

    List<NotificacionResponseDTO> listarUltimas10DelUsuarioAutenticado();

    void marcarComoLeida(Integer idNotificacion);
}
