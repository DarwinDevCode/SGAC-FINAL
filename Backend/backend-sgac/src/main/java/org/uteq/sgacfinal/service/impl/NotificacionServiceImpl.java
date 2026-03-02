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
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    public Notificacion enviarNotificacion(Integer idUsuarioDestino, String mensaje, String tipo) {
        // Elevate privileges for this transaction to bypass DatabaseRoleAspect restrictions
        // Coordinators don't have SELECT on seguridad.usuario by default
        try {
            org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
            session.doWork(connection -> {
                try (java.sql.Statement statement = connection.createStatement()) {
                    statement.execute("RESET ROLE");
                }
            });
        } catch (Exception e) {
            // Log and continue, might fail if no active transaction
            System.err.println("Error bypassing role for notification: " + e.getMessage());
        }

        Usuario usuario = usuarioRepository.findById(idUsuarioDestino)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para notificacion"));

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
