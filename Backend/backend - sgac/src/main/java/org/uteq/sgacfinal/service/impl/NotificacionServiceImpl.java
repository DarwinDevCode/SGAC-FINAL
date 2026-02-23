package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.Notificacion;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.NotificacionRepository;
import org.uteq.sgacfinal.service.INotificacionService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionServiceImpl implements INotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final IUsuariosRepository usuarioRepository;

    @Override
    public Notificacion enviarNotificacion(Integer idUsuarioDestino, String mensaje, String tipo) {
        Usuario usuario = usuarioRepository.findById(idUsuarioDestino)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Notificacion notificacion = Notificacion.builder()
                .usuarioDestino(usuario)
                .mensaje(mensaje)
                .tipo(tipo)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();

        return notificacionRepository.save(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> listarPorUsuario(Integer idUsuario) {
        return notificacionRepository.findByUsuarioDestino_IdUsuarioOrderByFechaEnvioDesc(idUsuario);
    }

    @Override
    public void marcarcomoLeida(Integer idNotificacion) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notificacion.setLeido(true);
        notificacionRepository.save(notificacion);
    }
}
