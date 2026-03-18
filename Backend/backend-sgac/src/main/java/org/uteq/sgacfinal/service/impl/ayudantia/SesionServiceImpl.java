package org.uteq.sgacfinal.service.impl.ayudantia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.EvidenciaRequest;
import org.uteq.sgacfinal.dto.Request.ayudantia.*;
import org.uteq.sgacfinal.dto.Response.ayudantia.*;
import org.uteq.sgacfinal.dto.ayudantia.SesionDTO; // Asegúrate de tener este DTO
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.exception.RecursoNoEncontradoException;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.repository.EvidenciaRegistroActividadRepository;
import org.uteq.sgacfinal.repository.ayudantia.*;
import org.uteq.sgacfinal.service.EmailService;
import org.uteq.sgacfinal.service.SesionService;
import org.uteq.sgacfinal.service.cloudinary.CloudinaryUploadServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SesionServiceImpl implements SesionService {

    private final RegistroActividadRepository registroRepository;
    private final AyudantiaRepository ayudantiaRepository;
    private final EvidenciaRegistroActividadRepository evidenciaRepository;

    // CAMBIO: Usamos el repositorio JPA para guardar las entidades de asistencia
    private final DetalleAsistenciaActividadRepository detalleAsistenciaRepository;

    private final CloudinaryUploadServiceImpl cloudinaryService;
    private final EmailService emailService;
    private final NotificacionSesionWsService wsService;

    @Override
    @Transactional
    public PlanificarSesionResponse planificarSesion(Integer idUsuarioAyudante, PlanificarSesionRequest request) {
        Integer idRegistroCreado = registroRepository.llamarFuncionPlanificar(
                request.getIdAyudantia(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getLugar(),
                request.getTemaTratado()
        );

        Ayudantia ayudantia = ayudantiaRepository.findById(request.getIdAyudantia())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ayudantía no encontrada"));

        // Ajuste de navegación: Postulacion no es Optional, es objeto directo
        Usuario docente = ayudantia.getPostulacion().getDocente().getUsuario();

        log.info("Sesión planificada para docente: {}", docente.getCorreo());

        return PlanificarSesionResponse.builder()
                .exito(true)
                .mensaje("Sesión planificada correctamente.")
                .idRegistroCreado(idRegistroCreado)
                .build();
    }

    @Override
    @Transactional
    public CompletarSesionResponse completarSesion(
            Integer idUsuarioAyudante,
            Integer idRegistroActividad,
            CompletarSesionRequest request,
            List<MultipartFile> archivos) {

        registroRepository.llamarFuncionCompletar(idRegistroActividad, request.getDescripcionActividad());

        RegistroActividad actividad = registroRepository.findById(idRegistroActividad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado"));

        // 2. Guardar Asistencias
        for (AsistenciaRequest asisReq : request.getAsistencias()) {
            DetalleAsistenciaActividad asistencia = new DetalleAsistenciaActividad();
            // Según tu entidad DetalleAsistenciaActividad, el campo es idRegistroActividad
            asistencia.setIdRegistroActividad(actividad);

            ParticipanteAyudantia p = new ParticipanteAyudantia();
            // Según tu entidad ParticipanteAyudantia, el campo es id
            p.setId(asisReq.getIdParticipanteAyudantia());
            asistencia.setIdParticipanteAyudantia(p);

            asistencia.setAsistio(asisReq.getAsistio());
            detalleAsistenciaRepository.save(asistencia);
        }

        // 3. Subir Evidencias
        for (int i = 0; i < archivos.size(); i++) {
            MultipartFile file = archivos.get(i);
            EvidenciaRequest meta = request.getMetadatosEvidencias().get(i);

            Map<String, Object> upload = cloudinaryService.upload(file);

            EvidenciaRegistroActividad evidencia = new EvidenciaRegistroActividad();
            evidencia.setRegistroActividad(actividad);
            evidencia.setNombreArchivo(meta.getNombreArchivo());
            evidencia.setRutaArchivo(upload.get("url").toString());
            evidencia.setMimeType(upload.get("mimeType").toString());
            evidencia.setTamanioBytes(((Long) upload.get("pesoBytes")).intValue());

            TipoEvidencia te = new TipoEvidencia();
            te.setId(meta.getIdTipoEvidencia());
            evidencia.setIdTipoEvidencia(te);

            // Falta idTipoEstadoEvidencia si es obligatorio en tu tabla
            TipoEstadoEvidencia estadoEv = new TipoEstadoEvidencia();
            estadoEv.setIdTipoEstadoEvidencia(1); // O el ID que corresponda a "SUBIDO"
            evidencia.setIdTipoEstadoEvidencia(estadoEv);

            evidenciaRepository.save(evidencia);
        }

        // 4. Notificaciones
        // NOTA: Quité el .get() porque Postulacion es un campo directo en Ayudantia
        Usuario docente = actividad.getAyudantia().getPostulacion().getDocente().getUsuario();
        Usuario ayudante = actividad.getAyudantia().getPostulacion().getAyudante().getUsuario();

        emailService.enviarNotificacionNuevaSesion(
                docente.getCorreo(),
                docente.getNombres() + " " + docente.getApellidos(),
                ayudante.getNombres() + " " + ayudante.getApellidos(),
                actividad.getAyudantia().getPostulacion().getAsignatura().getNombreAsignatura(),
                actividad.getFecha().toString(),
                actividad.getHorasDedicadas().toString(),
                actividad.getTemaTratado()
        );

        wsService.enviarNotificacion(docente.getIdUsuario(), "Nueva sesión registrada", "Revision pendiente", idRegistroActividad);

        return CompletarSesionResponse.builder().exito(true).mensaje("Sesión enviada.").build();
    }

    @Override
    @Transactional
    public EvaluarSesionResponse evaluarSesion(Integer idUsuarioDocente, Integer idRegistroActividad, EvaluarSesionRequest request) {
        registroRepository.llamarFuncionEvaluar(idRegistroActividad, request.getCodigoEstado(), request.getObservaciones());
        return EvaluarSesionResponse.builder().exito(true).mensaje("Evaluación registrada.").build();
    }

    // Implementación de los métodos faltantes de la interfaz para quitar el error de la línea 32
    @Override
    public List<SesionDTO> listarSesiones(Integer id, LocalDate f1, LocalDate f2, String s, Integer i) {
        return List.of();
    }

    @Override
    public SesionDTO detalleSesion(Integer idUsuario, Integer idRegistro) {
        // Implementar lógica de retorno de detalle
        return null;
    }
}