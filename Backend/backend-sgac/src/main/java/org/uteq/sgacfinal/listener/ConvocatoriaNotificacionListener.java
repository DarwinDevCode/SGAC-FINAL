package org.uteq.sgacfinal.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.repository.DecanoNotificacionRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.impl.ConvocatoriaNotificacionAsyncService;
import org.uteq.sgacfinal.event.ConvocatoriaCreadaEvent;

import java.util.Optional;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConvocatoriaNotificacionListener {

    private final IConvocatoriaRepository convocatoriaRepository;
    private final INotificacionService notificacionService;
    private final DecanoNotificacionRepository decanoNotificacionRepository;
    private final ConvocatoriaNotificacionAsyncService convocatoriaNotificacionAsyncService;

    /**
     * Se ejecuta solo si la transacción de creación de convocatoria COMMITea exitosamente.
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onConvocatoriaCreada(ConvocatoriaCreadaEvent event) {
        if (event == null || event.getIdConvocatoria() == null) {
            log.warn("[NOTIF-CONV] Event null o sin idConvocatoria");
            return;
        }

        log.info("[NOTIF-CONV] AFTER_COMMIT recibido para idConvocatoria={}", event.getIdConvocatoria());

        Optional<Convocatoria> opt = convocatoriaRepository.findById(event.getIdConvocatoria());
        if (opt.isEmpty()) {
            log.warn("[NOTIF-CONV] No se encontró convocatoria id={}", event.getIdConvocatoria());
            return;
        }

        Convocatoria convocatoria = opt.get();
        if (convocatoria.getAsignatura() == null) {
            log.warn("[NOTIF-CONV] Convocatoria {} sin asignatura", convocatoria.getIdConvocatoria());
            return;
        }

        Integer idConvocatoria = convocatoria.getIdConvocatoria();
        String nombreAsignatura = convocatoria.getAsignatura().getNombreAsignatura();
        Integer idCarrera = convocatoria.getAsignatura().getCarrera() != null ? convocatoria.getAsignatura().getCarrera().getIdCarrera() : null;
        String nombreCarrera = convocatoria.getAsignatura().getCarrera() != null ? convocatoria.getAsignatura().getCarrera().getNombreCarrera() : null;

        // 1) Docente responsable
        if (convocatoria.getDocente() != null && convocatoria.getDocente().getUsuario() != null
                && convocatoria.getDocente().getUsuario().getIdUsuario() != null) {

            Integer idUsuarioDocente = convocatoria.getDocente().getUsuario().getIdUsuario();
            log.info("[NOTIF-CONV] Notificando docente idUsuario={}", idUsuarioDocente);

            NotificationRequest reqDocente = NotificationRequest.builder()
                    .titulo("Nueva convocatoria")
                    .mensaje("Has sido asignado como responsable de una nueva convocatoria de ayudantía.")
                    .tipo("CONVOCATORIA")
                    .idReferencia(idConvocatoria)
                    .build();

            try {
                notificacionService.enviarNotificacion(idUsuarioDocente, reqDocente);
            } catch (Exception e) {
                log.error("[NOTIF-CONV] Error notificando docente idUsuario={}", idUsuarioDocente, e);
            }
        } else {
            log.warn("[NOTIF-CONV] Convocatoria {} sin docente/usuario asociado", idConvocatoria);
        }

        // 2) Decano
        Integer idFacultad = (convocatoria.getAsignatura().getCarrera() != null
                && convocatoria.getAsignatura().getCarrera().getFacultad() != null)
                ? convocatoria.getAsignatura().getCarrera().getFacultad().getIdFacultad()
                : null;

        if (idFacultad != null) {
            decanoNotificacionRepository.findIdUsuarioDecanoActivoByFacultad(idFacultad)
                    .ifPresentOrElse(idUsuarioDecano -> {
                        log.info("[NOTIF-CONV] Notificando decano idUsuario={} (idFacultad={})", idUsuarioDecano, idFacultad);
                        NotificationRequest reqDecano = NotificationRequest.builder()
                                .titulo("Nueva convocatoria")
                                .mensaje("Se ha publicado una nueva convocatoria en la carrera de " + (nombreCarrera != null ? nombreCarrera : "tu carrera") + ".")
                                .tipo("CONVOCATORIA")
                                .idReferencia(idConvocatoria)
                                .build();
                        try {
                            notificacionService.enviarNotificacion(idUsuarioDecano, reqDecano);
                        } catch (Exception e) {
                            log.error("[NOTIF-CONV] Error notificando decano idUsuario={}", idUsuarioDecano, e);
                        }
                    }, () -> log.warn("[NOTIF-CONV] No hay decano activo para idFacultad={}", idFacultad));
        } else {
            log.warn("[NOTIF-CONV] No se pudo resolver idFacultad para convocatoria {}", idConvocatoria);
        }

        // 3) Estudiantes (async)
        if (idCarrera != null) {
            log.info("[NOTIF-CONV] Disparando async a estudiantes (idCarrera={})", idCarrera);
            convocatoriaNotificacionAsyncService.notificarEstudiantesNuevaConvocatoria(idCarrera, nombreAsignatura, idConvocatoria);
        } else {
            log.warn("[NOTIF-CONV] No se pudo resolver idCarrera para convocatoria {}", idConvocatoria);
        }
    }
}
