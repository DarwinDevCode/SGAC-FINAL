package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Response.NotificacionResponseDTO;
import org.uteq.sgacfinal.entity.NotificacionW;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.NotificacionWRepository;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.IUsuarioSesionService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificacionServiceImpl implements INotificacionService {

    private final NotificacionWRepository notificacionRepository;
    private final IUsuariosRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final IUsuarioSesionService usuarioSesionService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificacionResponseDTO enviarNotificacion(Integer idUsuario, NotificationRequest request) {
        log.info("[NOTIF-SVC] -> enviarNotificacion(idUsuario={}, tipo={}, idReferencia={}) txActive={} txName={} thread={}",
                idUsuario,
                request != null ? request.getTipo() : null,
                request != null ? request.getIdReferencia() : null,
                TransactionSynchronizationManager.isActualTransactionActive(),
                TransactionSynchronizationManager.getCurrentTransactionName(),
                Thread.currentThread().getName());

        Usuario usuario = usuarioRepository.findById(idUsuario.intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        NotificacionW notificacion = new NotificacionW();
        notificacion.setIdUsuario(usuario);
        notificacion.setTitulo(request.getTitulo());
        notificacion.setMensaje(request.getMensaje());
        notificacion.setTipo(request.getTipo());
        notificacion.setIdReferencia(request.getIdReferencia());
        notificacion.setLeido(false);
        notificacion.setFechaCreacion(Instant.now());

        log.info("[NOTIF-SVC] Persistiendo notificación para idUsuario={} (antes de save)", idUsuario);

        NotificacionW saved = notificacionRepository.save(notificacion);

        log.info("[NOTIF-SVC] Persistida notificación idNotificacion={} (después de save)", saved.getId());

        boolean exists = saved.getId() != null && notificacionRepository.existsById(saved.getId());
        log.info("[NOTIF-SVC] Verificación inmediata existsById({}) => {}", saved.getId(), exists);

        NotificacionResponseDTO payload = mapToDto(saved);

        log.info("[NOTIF-SVC] Enviando WS destino=/queue/notificaciones/{}", idUsuario);
        messagingTemplate.convertAndSend("/queue/notificaciones/" + idUsuario, payload);
        log.info("[NOTIF-SVC] <- enviarNotificacion OK idNotificacion={}", saved.getId());

        return payload;
    }


    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarUltimas10DelUsuarioAutenticado() {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        return notificacionRepository
                .findByIdUsuario_IdUsuarioOrderByFechaCreacionDesc(idUsuario, PageRequest.of(0, 10))
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void marcarComoLeida(Integer idNotificacion) {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        NotificacionW notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        // Validar ownership
        if (notificacion.getIdUsuario() == null || notificacion.getIdUsuario().getIdUsuario() == null
                || !notificacion.getIdUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("No autorizado para modificar esta notificación");
        }

        if (Boolean.TRUE.equals(notificacion.getLeido())) {
            return;
        }

        notificacion.setLeido(true);
        notificacion.setFechaLectura(Instant.now());
        notificacionRepository.save(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarNoLeidasDelUsuarioAutenticado(Integer limit) {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        List<NotificacionW> list = notificacionRepository
                .findByIdUsuario_IdUsuarioAndLeidoFalseOrderByFechaCreacionDesc(idUsuario);

        if (limit != null && limit > 0 && list.size() > limit) {
            list = list.subList(0, limit);
        }

        return list.stream().map(this::mapToDto).toList();
    }

    private NotificacionResponseDTO mapToDto(NotificacionW n) {
        return NotificacionResponseDTO.builder()
                .idNotificacion(n.getId())
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
