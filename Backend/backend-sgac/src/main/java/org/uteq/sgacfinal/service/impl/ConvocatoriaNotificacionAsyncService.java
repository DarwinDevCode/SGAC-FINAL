package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.repository.EstudianteNotificacionRepository;
import org.uteq.sgacfinal.service.INotificacionService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocatoriaNotificacionAsyncService {

    private final EstudianteNotificacionRepository estudianteNotificacionRepository;
    private final INotificacionService notificacionService;


    @Async
    @Transactional(readOnly = true)
    public void notificarEstudiantesNuevaConvocatoria(Integer idCarrera, String nombreAsignatura, Integer idConvocatoria) {
        log.info("[NOTIF-CONV] (async) Buscando estudiantes por idCarrera={} para convocatoria={}", idCarrera, idConvocatoria);

        List<Integer> idsUsuario = estudianteNotificacionRepository.findIdsUsuarioActivosByCarrera(idCarrera);

        if (idsUsuario == null || idsUsuario.isEmpty()) {
            log.warn("[NOTIF-CONV] (async) No hay estudiantes activos para notificar en idCarrera={}", idCarrera);
            return;
        }

        log.info("[NOTIF-CONV] (async) Estudiantes activos a notificar: {}", idsUsuario.size());

        NotificationRequest req = NotificationRequest.builder()
                .titulo("Nueva convocatoria")
                .mensaje("Nueva convocatoria disponible para la asignatura " + nombreAsignatura + ". ¡Postúlate ahora!")
                .tipo("CONVOCATORIA")
                .idReferencia(idConvocatoria)
                .build();

        for (Integer idUsuario : idsUsuario) {
            if (idUsuario == null) continue;
            try {
                notificacionService.enviarNotificacion(idUsuario, req);
            } catch (Exception e) {
                log.error("[NOTIF-CONV] (async) Error notificando estudiante idUsuario={}", idUsuario, e);
            }
        }
    }
}
