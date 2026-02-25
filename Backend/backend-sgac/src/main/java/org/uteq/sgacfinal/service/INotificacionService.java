package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.entity.Notificacion;

import java.util.List;

public interface INotificacionService {
    Notificacion enviarNotificacion(Integer idUsuarioDestino, String mensaje, String tipo);
    List<Notificacion> listarPorUsuario(Integer idUsuario);
    void marcarcomoLeida(Integer idNotificacion);
}
