package org.uteq.sgacfinal.service.impl.ayudantia;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.EvidenciaRequest;
import org.uteq.sgacfinal.dto.request.CompletarSesionRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarSesionRequest;
import org.uteq.sgacfinal.dto.request.PlanificarSesionRequest;
import org.uteq.sgacfinal.dto.response.SesionDTO;
import org.uteq.sgacfinal.dto.response.CompletarSesionResponseDTO;
import org.uteq.sgacfinal.dto.response.EvaluarSesionResponse;
import org.uteq.sgacfinal.dto.response.PlanificarSesionResponseDTO;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.exception.BadRequestException;
import org.uteq.sgacfinal.exception.RecursoNoEncontradoException;
import org.uteq.sgacfinal.repository.EvidenciaRegistroActividadRepository;
import org.uteq.sgacfinal.repository.ayudantia.AsistenciaRepository;
import org.uteq.sgacfinal.repository.ayudantia.RegistroActividadRepository;
import org.uteq.sgacfinal.repository.ayudantia.SesionRepository;
import org.uteq.sgacfinal.service.EmailService;
import org.uteq.sgacfinal.service.ayudantia.SesionService;
import org.uteq.sgacfinal.repository.ProgresoRepository;
import org.uteq.sgacfinal.dto.response.ProgresoGeneralResponse;
import org.uteq.sgacfinal.dto.response.ControlSemanalResponse;
import java.math.BigDecimal;
import org.uteq.sgacfinal.service.cloudinary.CloudinaryUploadServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SesionServiceConfigImpl implements SesionService {

    private final RegistroActividadRepository registroRepo;
    private final SesionRepository sesionRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final EvidenciaRegistroActividadRepository evidenciaRepo;
    private final CloudinaryUploadServiceImpl cloudinary;
    private final EmailService emailService;
    private final NotificacionSesionWsService wsService;
    private final ObjectMapper objectMapper;
    private final ProgresoRepository progresoRepository;

    @Override
    @Transactional
    public PlanificarSesionResponseDTO planificarSesion(
            Integer idUsuarioAyudante,
            PlanificarSesionRequest request) {

        Integer idRegistroCreado = registroRepo.llamarFuncionPlanificar(
                request.getIdAyudantia(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getLugar(),
                request.getTemaTratado()
        );

        log.error("[SesionService] ID de registro creado: {}", idRegistroCreado);


        RegistroActividad actividad = registroRepo.findById(idRegistroCreado).
                orElseThrow(() -> new RecursoNoEncontradoException(
                        "Registro creado no encontrado: " + idRegistroCreado));

        try {
            Usuario docente = resolverDocente(actividad);
            Usuario ayudante = resolverAyudante(actividad);
            String asignatura = resolverAsignatura(actividad);

            emailService.enviarNotificacionNuevaSesion(
                    docente.getCorreo(),
                    docente.getNombres() + " " + docente.getApellidos(),
                    ayudante.getNombres() + " " + ayudante.getApellidos(),
                    asignatura,
                    request.getFecha().toString(),
                    request.getHoraInicio().toString(),
                    request.getHoraFin().toString(),
                    actividad.getHorasDedicadas() != null
                            ? actividad.getHorasDedicadas().toString() + " h" : "—",
                    request.getTemaTratado()
            );

            wsService.enviarNotificacion(
                    docente.getIdUsuario(),
                    "Nueva sesión planificada",
                    String.format("Ayudante %s planificó una sesión el %s en %s",
                            ayudante.getNombres(), request.getFecha(), request.getLugar()),
                    idRegistroCreado
            );
        } catch (Exception ex) {
            log.warn("[SesionService] Fallo al notificar planificación {}: {}",
                    idRegistroCreado, ex.getMessage());
        }

        return PlanificarSesionResponseDTO.builder()
                .exito(true)
                .mensaje("Sesión planificada correctamente. El docente ha sido notificado.")
                .idRegistroCreado(idRegistroCreado)
                .build();
    }

    @Override
    @Transactional
    public CompletarSesionResponseDTO completarSesion(
            Integer idUsuarioAyudante,
            Integer idRegistroActividad,
            CompletarSesionRequestDTO request,
            List<MultipartFile> archivos) {

        if (request.getAsistencias() != null && !request.getAsistencias().isEmpty()) {
            try {
                List<Map<String, Object>> payload = request.getAsistencias().stream()
                        .map(a -> Map.<String, Object>of(
                                "idParticipante", a.getIdParticipanteAyudantia(),
                                "asistio",        a.getAsistio()))
                        .collect(Collectors.toList());

                String asistenciasJson = objectMapper.writeValueAsString(payload);
                asistenciaRepo.guardarAsistencias(idRegistroActividad, asistenciasJson);

            } catch (Exception ex) {
                log.error("[SesionService] Error guardando asistencias para {}", idRegistroActividad, ex);
                throw new BadRequestException("Error al guardar las asistencias: " + ex.getMessage());
            }
        }

        RegistroActividad actividad = registroRepo.findById(idRegistroActividad)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Registro de actividad no encontrado: " + idRegistroActividad));

        if (archivos != null && !archivos.isEmpty()) {

            // ── CORRECCIÓN CLAVE ─────────────────────────────────────────
            // CompletarSesionRequest.getMetadatosEvidencias() devuelve
            // List<org.uteq.sgacfinal.dto.EvidenciaRequest>.
            // El tipo en el import de arriba ya coincide con eso,
            // por lo que este cast ahora compila sin error.
            List<EvidenciaRequest> metas = request.getMetadatosEvidencias() != null
                    ? request.getMetadatosEvidencias()
                    : new ArrayList<>();

            for (int i = 0; i < archivos.size(); i++) {
                MultipartFile archivo = archivos.get(i);
                if (archivo == null || archivo.isEmpty()) continue;

                try {
                    Map<String, Object> uploadResult = cloudinary.upload(archivo);

                    EvidenciaRegistroActividad evidencia = new EvidenciaRegistroActividad();
                    evidencia.setRegistroActividad(actividad);
                    evidencia.setNombreArchivo(archivo.getOriginalFilename());
                    evidencia.setRutaArchivo(uploadResult.get("url").toString());
                    evidencia.setMimeType(uploadResult.get("mimeType") != null
                            ? uploadResult.get("mimeType").toString() : archivo.getContentType());
                    evidencia.setFechaSubida(LocalDate.now());
                    evidencia.setActivo(true);

                    Object tamBytes = uploadResult.get("pesoBytes");
                    if (tamBytes instanceof Long l)         evidencia.setTamanioBytes(l.intValue());
                    else if (tamBytes instanceof Integer n) evidencia.setTamanioBytes(n);

                    // EvidenciaRequest (dto raíz) expone getNombreArchivo(), no
                    // getNombreArchivoReferencia(), por eso usamos el getter correcto
                    if (i < metas.size() && metas.get(i).getIdTipoEvidencia() != null) {
                        TipoEvidencia te = new TipoEvidencia();
                        te.setId(metas.get(i).getIdTipoEvidencia());
                        evidencia.setIdTipoEvidencia(te);
                    }

                    // Estado inicial de la evidencia: SUBIDO (id = 1 por convención)
                    TipoEstadoEvidencia estadoEv = new TipoEstadoEvidencia();
                    estadoEv.setId(1);
                    evidencia.setIdTipoEstadoEvidencia(estadoEv);

                    evidenciaRepo.save(evidencia);

                } catch (Exception ex) {
                    log.error("[SesionService] Error subiendo archivo {} para registro {}",
                            archivo.getOriginalFilename(), idRegistroActividad, ex);
                    throw new BadRequestException(
                            "Error al subir el archivo " + archivo.getOriginalFilename()
                                    + ": " + ex.getMessage());
                }
            }
        }

        // ── 3. Transición de estado: PLANIFICADO/RECHAZADO → PENDIENTE ────
        Boolean transicionOk = registroRepo.llamarFuncionCompletar(
                idRegistroActividad,
                request.getDescripcionActividad());

        if (Boolean.FALSE.equals(transicionOk)) {
            throw new BadRequestException(
                    "No se pudo cambiar el estado de la sesión a PENDIENTE.");
        }

        // ── 4. Notificar al docente ────────────────────────────────────────
        try {
            Usuario docente  = resolverDocente(actividad);
            Usuario ayudante = resolverAyudante(actividad);

            wsService.enviarNotificacion(
                    docente.getIdUsuario(),
                    "Sesión enviada a revisión",
                    String.format("El ayudante %s envió la sesión del %s para su revisión.",
                            ayudante.getNombres() + " " + ayudante.getApellidos(),
                            actividad.getFecha()),
                    idRegistroActividad
            );
        } catch (Exception ex) {
            log.warn("[SesionService] Fallo al notificar completar {}: {}",
                    idRegistroActividad, ex.getMessage());
        }

        return CompletarSesionResponseDTO.builder()
                .exito(true)
                .mensaje("Sesión enviada a revisión correctamente.")
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════
    // EVALUAR
    // ═════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public EvaluarSesionResponse evaluarSesion(
            Integer idUsuarioDocente,
            Integer idRegistroActividad,
            EvaluarSesionRequest request) {

        Boolean ok = registroRepo.llamarFuncionEvaluar(
                idRegistroActividad,
                request.getCodigoEstado(),
                request.getObservaciones());

        if (Boolean.FALSE.equals(ok)) {
            throw new BadRequestException("No se pudo registrar la evaluación.");
        }

        try {
            RegistroActividad actividad = registroRepo.findById(idRegistroActividad)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado"));
            Usuario ayudante = resolverAyudante(actividad);

            boolean aprobado = "APROBADO".equalsIgnoreCase(request.getCodigoEstado());
            wsService.enviarNotificacion(
                    ayudante.getIdUsuario(),
                    aprobado ? "Sesión aprobada" : "Sesión devuelta para corrección",
                    aprobado
                            ? "Tu sesión del " + actividad.getFecha() + " fue aprobada. ¡Bien hecho!"
                            : "Tu sesión requiere correcciones: " + request.getObservaciones(),
                    idRegistroActividad
            );
        } catch (Exception ex) {
            log.warn("[SesionService] Fallo al notificar evaluación {}: {}",
                    idRegistroActividad, ex.getMessage());
        }

        return EvaluarSesionResponse.builder()
                .exito(true)
                .mensaje(switch (request.getCodigoEstado().toUpperCase()) {
                    case "APROBADO"  -> "Sesión aprobada. Las horas han sido acumuladas.";
                    case "RECHAZADO" -> "Sesión devuelta para corrección.";
                    default          -> "Evaluación registrada.";
                })
                .build();
    }

    @Override
    public List<SesionDTO> listarSesiones(
            Integer   idUsuario,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String    estadoCodigo,
            Integer   idAyudantiaSobrescrito) {

        Integer idAyudantia = idAyudantiaSobrescrito != null
                ? idAyudantiaSobrescrito
                : sesionRepo.obtenerIdAyudantiaPorUsuario(idUsuario);

        return sesionRepo.listarSesiones(idAyudantia, fechaDesde, fechaHasta, estadoCodigo);
    }

    // ═════════════════════════════════════════════════════════════════════
    // DETALLE
    // ═════════════════════════════════════════════════════════════════════

    @Override
    public SesionDTO detalleSesion(Integer idUsuario, Integer idRegistroActividad) {

        RegistroActividad ra = registroRepo.findById(idRegistroActividad)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Sesión no encontrada: " + idRegistroActividad));

        List<SesionDTO.EvidenciaDTO> evidenciaDTOs = ra.getEvidencias() == null
                ? List.of()
                : ra.getEvidencias().stream()
                .filter(ev -> Boolean.TRUE.equals(ev.getActivo()))
                .map(ev -> SesionDTO.EvidenciaDTO.builder()
                        .idEvidenciaRegistroActividad(ev.getIdEvidenciaRegistroActividad())
                        .nombreArchivo(ev.getNombreArchivo())
                        .rutaArchivo(ev.getRutaArchivo())
                        .mimeType(ev.getMimeType())
                        .fechaSubida(ev.getFechaSubida())
                        .codigoEstadoEvidencia(
                                ev.getIdTipoEstadoEvidencia() != null
                                        ? ev.getIdTipoEstadoEvidencia().getCodigo() : null)
                        .nombreEstadoEvidencia(
                                ev.getIdTipoEstadoEvidencia() != null
                                        ? ev.getIdTipoEstadoEvidencia().getNombreEstado() : null)
                        .observacionDocente(ev.getObservaciones())
                        .fechaObservacion(ev.getFechaObservacion())
                        .build())
                .collect(Collectors.toList());

        TipoEstadoRegistro estado = ra.getIdTipoEstadoRegistro();

        return SesionDTO.builder()
                .idRegistroActividad(ra.getIdRegistroActividad())
                .fecha(ra.getFecha())
                .horaInicio(ra.getHoraInicio())
                .horaFin(ra.getHoraFin())
                .horasDedicadas(ra.getHorasDedicadas())
                .temaTratado(ra.getTemaTratado())
                .lugar(ra.getLugar())
                .descripcionActividad(ra.getDescripcionActividad())
                .observacionDocente(ra.getObservaciones())
                .fechaObservacion(ra.getFechaObservacion())
                .codigoEstado(estado != null ? estado.getCodigo()       : null)
                .nombreEstado(estado != null ? estado.getNombreEstado() : null)
                .evidencias(evidenciaDTOs)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════
    // HELPERS — Navegación correcta del grafo de entidades
    // ═════════════════════════════════════════════════════════════════════

    @Override
    public ProgresoGeneralResponse progresoGeneral(Integer idUsuario) {
        // Resolvemos el ID de ayudantía para este usuario, ya que las funciones DB suelen esperarlo
        Integer idAyudantia = sesionRepo.obtenerIdAyudantiaPorUsuario(idUsuario);
        
        List<Object[]> resultado = progresoRepository.progresoGeneral(idAyudantia);
        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No se encontró información de progreso para la ayudantía del usuario: " + idUsuario
            );
        }
        return mapearProgresoGeneral(resultado.get(0));
    }

    @Override
    public ControlSemanalResponse controlSemanal(Integer idUsuario) {
        // Resolvemos el ID de ayudantía para este usuario
        Integer idAyudantia = sesionRepo.obtenerIdAyudantiaPorUsuario(idUsuario);
        
        List<Object[]> resultado = progresoRepository.controlSemanal(idAyudantia);
        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No se encontró información de control semanal para la ayudantía del usuario: " + idUsuario
            );
        }
        return mapearControlSemanal(resultado.get(0));
    }

    private ProgresoGeneralResponse mapearProgresoGeneral(Object[] fila) {
        return ProgresoGeneralResponse.builder()
                .horasAprobadas(toBigDecimal(    fila[0]))
                .horasPendientes(toBigDecimal(   fila[1]))
                .horasObservadas(toBigDecimal(   fila[2]))
                .horasTotalesRegistradas(toBigDecimal(fila[3]))
                .horasMaximas(toBigDecimal(      fila[4]))
                .porcentajeAvance(toBigDecimal(  fila[5]))
                .totalSesiones(toLong(           fila[6]))
                .sesionesAprobadas(toLong(       fila[7]))
                .sesionesPendientes(toLong(      fila[8]))
                .sesionesObservadas(toLong(      fila[9]))
                .build();
    }

    private ControlSemanalResponse mapearControlSemanal(Object[] fila) {
        return ControlSemanalResponse.builder()
                .semanaInicio(toLocalDate(       fila[0]))
                .semanaFin(toLocalDate(          fila[1]))
                .horasRegistradas(toBigDecimal(  fila[2]))
                .horasAprobadasSemana(toBigDecimal(fila[3]))
                .horasPendientesSemana(toBigDecimal(fila[4]))
                .limiteSemanal(toBigDecimal(     fila[5]))
                .horasDisponibles(toBigDecimal(  fila[6]))
                .superaLimite(toBoolean(         fila[7]))
                .sesionesSemana(toLong(          fila[8]))
                .build();
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return new BigDecimal(((Number) o).doubleValue());
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long) return (Long) o;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private Boolean toBoolean(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).intValue() != 0;
        return Boolean.parseBoolean(o.toString());
    }

    private LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof java.sql.Date) return ((java.sql.Date) o).toLocalDate();
        if (o instanceof java.sql.Timestamp) return ((java.sql.Timestamp) o).toLocalDateTime().toLocalDate();
        if (o instanceof LocalDate) return (LocalDate) o;
        try { return LocalDate.parse(o.toString()); } catch (Exception e) { return null; }
    }

    private Usuario resolverDocente(RegistroActividad actividad) {
        return actividad
                .getAyudantia()
                .getPostulacion()
                .getConvocatoria()
                .getDocente()
                .getUsuario();
    }

    private Usuario resolverAyudante(RegistroActividad actividad) {
        return actividad
                .getAyudantia()
                .getPostulacion()
                .getEstudiante()
                .getUsuario();
    }

    private String resolverAsignatura(RegistroActividad actividad) {
        return actividad
                .getAyudantia()
                .getPostulacion()
                .getConvocatoria()
                .getAsignatura()
                .getNombreAsignatura();
    }
}