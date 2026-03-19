package org.uteq.sgacfinal.service.impl.ayudantia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.dto.request.ayudantia.PlanificarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.AsistenciaSesionActualResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.MarcadoAsistenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.PlanificacionResponseDTO;
import org.uteq.sgacfinal.entity.RegistroActividad;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.ayudantia.AsistenciaSesionRepository;
import org.uteq.sgacfinal.repository.ayudantia.RegistroActividadRepository;
import org.uteq.sgacfinal.service.EmailService;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.ayudantia.AsistenciaSesionService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenciaSesionServiceImpl implements AsistenciaSesionService {

    private final AsistenciaSesionRepository asistenciaRepository;
    private final RegistroActividadRepository registroRepo;
    private final EmailService emailService;
    private final INotificacionService notificacionService;

    @Override
    @Transactional
    public RespuestaOperacionDTO<PlanificacionResponseDTO> planificarSesion(PlanificarSesionRequestDTO request) {
        RespuestaOperacionDTO<PlanificacionResponseDTO> response = asistenciaRepository.planificarSesion(request);

        if (response.valido() && response.datos() != null) {
            Integer idRegistro = response.datos().idRegistro();
            notificarDocente(idRegistro, "PLANIFICACION", request);
        }

        return response;
    }

    @Override
    public RespuestaOperacionDTO<AsistenciaSesionActualResponseDTO> obtenerAsistenciaSesionActual(Integer idUsuario) {
        return asistenciaRepository.obtenerAsistenciaSesionActual(idUsuario);
    }

    @Override
    public RespuestaOperacionDTO<Void> marcarAsistencia(MarcadoAsistenciaRequestDTO request) {
        return asistenciaRepository.marcarAsistencia(request);
    }

    private void notificarDocente(Integer idRegistro, String tipo, PlanificarSesionRequestDTO request) {
        try {
            RegistroActividad actividad = registroRepo.findById(idRegistro).orElseThrow();
            Usuario docente = actividad.getAyudantia().getPostulacion().getConvocatoria().getDocente().getUsuario();
            Usuario ayudante = actividad.getAyudantia().getPostulacion().getEstudiante().getUsuario();
            String asignatura = actividad.getAyudantia().getPostulacion().getConvocatoria().getAsignatura().getNombreAsignatura();
            String nombreAyudante = ayudante.getNombres() + " " + ayudante.getApellidos();

            emailService.enviarNotificacionNuevaSesion(
                    docente.getCorreo(),
                    docente.getNombres() + " " + docente.getApellidos(),
                    nombreAyudante,
                    asignatura,
                    request.fecha().toString(),
                    request.horaInicio().toString(),
                    request.horaFin().toString(),
                    actividad.getHorasDedicadas() != null ? actividad.getHorasDedicadas().toString() + " h" : "—",
                    request.tema()
            );

            NotificationRequest wsReq = new NotificationRequest();
            wsReq.setTitulo("Nueva sesión planificada");
            wsReq.setMensaje(String.format("El ayudante %s ha planificado una sesión el %s en %s", nombreAyudante, request.fecha(), request.lugar()));
            wsReq.setTipo("INFO");
            wsReq.setIdReferencia(idRegistro);
            notificacionService.enviarNotificacion(docente.getIdUsuario(), wsReq);

        } catch (Exception ex) {
            log.error("[AsistenciaSesionService] Error al notificar al docente sobre la sesión {}: {}", idRegistro, ex.getMessage());
        }
    }
}