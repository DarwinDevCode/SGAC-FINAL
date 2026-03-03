package org.uteq.sgacfinal.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Response.NotificacionResponseDTO;
import org.uteq.sgacfinal.entity.Notificacion;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.NotificacionRepository;
import org.uteq.sgacfinal.service.INotificacionService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionServiceImpl implements INotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final IUsuariosRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EntityManager entityManager;


    @Override
    public Notificacion enviarNotificacion(Integer idUsuarioDestino, String mensaje, String tipo) {

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
                .usuario(usuario)
                .titulo(request.getTitulo())
                .mensaje(request.getMensaje())
                .tipo(request.getTipo())
                .idReferencia(request.getIdReferencia())
                .leido(false)
                .fechaCreacion(Instant.now())
                .build();

        Notificacion saved = notificacionRepository.save(notificacion);

        NotificacionResponseDTO payload = mapToDto(saved);

        // Envío en tiempo real (privado por usuario)
        messagingTemplate.convertAndSend("/queue/notificaciones/" + idUsuario, payload);

        return payload;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarUltimas10DelUsuarioAutenticado() {
        Integer idUsuario = getIdUsuarioAutenticado();

        return notificacionRepository
                .findByUsuario_IdUsuarioOrderByFechaCreacionDesc(idUsuario, PageRequest.of(0, 10))
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void marcarComoLeida(Integer idNotificacion) {
        Integer idUsuario = getIdUsuarioAutenticado();

        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        // Validar ownership
        if (notificacion.getUsuario() == null || notificacion.getUsuario().getIdUsuario() == null
                || !notificacion.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("No autorizado para modificar esta notificación");
        }

        if (Boolean.TRUE.equals(notificacion.getLeido())) {
            return;
        }

        notificacion.setLeido(true);
        notificacion.setFechaLectura(Instant.now());
        notificacionRepository.save(notificacion);
    }

    private Integer getIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        Object principal = authentication.getPrincipal();

        // Caso normal de este proyecto: UsuarioPrincipal envuelve Usuario pero no expone getter.
        if (principal instanceof org.uteq.sgacfinal.security.UsuarioPrincipal usuarioPrincipal) {
            try {
                java.lang.reflect.Field f = org.uteq.sgacfinal.security.UsuarioPrincipal.class.getDeclaredField("usuario");
                f.setAccessible(true);
                Usuario u = (Usuario) f.get(usuarioPrincipal);
                if (u != null && u.getIdUsuario() != null) {
                    return u.getIdUsuario();
                }
            } catch (Exception ignored) {
                // fallback abajo
            }
        }

        if (principal instanceof UserDetails userDetails) {
            return usuarioRepository.findByNombreUsuarioWithRolesAndTipoRol(userDetails.getUsername())
                    .map(Usuario::getIdUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
        }

        throw new RuntimeException("No hay usuario autenticado");
    }

    private NotificacionResponseDTO mapToDto(Notificacion n) {
        return NotificacionResponseDTO.builder()
                .idNotificacion(n.getIdNotificacion())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .tipo(n.getTipo())
                .idReferencia(n.getIdReferencia())
                .leido(n.getLeido())
                .fechaCreacion(n.getFechaCreacion())
                .fechaLectura(n.getFechaLectura())
                .build();
    }
}
