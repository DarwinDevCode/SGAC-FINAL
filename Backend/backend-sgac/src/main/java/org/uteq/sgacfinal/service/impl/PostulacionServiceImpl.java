package org.uteq.sgacfinal.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Request.PostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.PostulacionResponseDTO;
import org.uteq.sgacfinal.entity.Estudiante;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.EstudianteRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.IPostulacionService;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostulacionServiceImpl implements IPostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final INotificacionService notificacionService;

    public PostulacionServiceImpl(PostulacionRepository postulacionRepository,
                                  EstudianteRepository estudianteRepository,
                                  INotificacionService notificacionService) {
        this.postulacionRepository = postulacionRepository;
        this.estudianteRepository = estudianteRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    public PostulacionResponseDTO crear(PostulacionRequestDTO request) {
        Estudiante estudiante = estudianteRepository.findByUsuario_IdUsuario(request.getIdEstudiante())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado para el usuario con ID: " + request.getIdEstudiante()));

        long activas = postulacionRepository.contarPostulacionesActivasPorEstudiante(estudiante.getIdEstudiante());
        if (activas > 0) {
            throw new RuntimeException("Ya tienes una postulacion activa. No puedes postularte a mas de una convocatoria a la vez.");
        }

        Integer idGenerado = postulacionRepository.crearPostulacion(
                request.getIdConvocatoria(),
                estudiante.getIdEstudiante(),
                new Date(System.currentTimeMillis()),
                "PENDIENTE",
                request.getObservaciones());

        if (idGenerado == null || idGenerado == -1) {
            throw new RuntimeException("Error al crear la postulacion.");
        }

        Postulacion entidad = postulacionRepository.findById(idGenerado)
                .orElseGet(() -> postulacionRepository.findByIdPostulacion(idGenerado));

        if (entidad == null) {
            throw new RuntimeException("No se pudo recuperar la postulacion recien creada.");
        }

        return mapearADTO(entidad);
    }

    @Override
    public PostulacionResponseDTO actualizar(Integer id, PostulacionRequestDTO request) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulacion no encontrada con ID: " + id));

        if (request.getObservaciones() != null) {
            postulacion.setObservaciones(request.getObservaciones());
        }

        postulacion = postulacionRepository.save(postulacion);
        return mapearADTO(postulacion);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = postulacionRepository.desactivarPostulacion(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al anular la postulacion.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PostulacionResponseDTO buscarPorId(Integer id) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulacion no encontrada con ID: " + id));
        return mapearADTO(postulacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorEstudiante(Integer idUsuario) {
        Optional<Estudiante> estudianteOpt = estudianteRepository.findByUsuario_IdUsuario(idUsuario);
        if (estudianteOpt.isEmpty()) {
            return List.of();
        }

        List<Object[]> resultados = postulacionRepository
                .listarPostulacionesPorEstudianteSP(estudianteOpt.get().getIdEstudiante());

        return resultados.stream()
                .map(this::mapearDesdeObjectArray)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorConvocatoria(Integer idConvocatoria) {
        return postulacionRepository.findByConvocatoria_IdConvocatoria(idConvocatoria).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorCarrera(Integer idCarrera) {
        return postulacionRepository.findByCarrera(idCarrera).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPendientesPorCarrera(Integer idCarrera) {
        return postulacionRepository.findByEstadoAndCarrera("PENDIENTE", idCarrera).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarEnEvaluacionPorCarrera(Integer idCarrera) {
        return postulacionRepository.findByEstadoAndCarrera("EN_EVALUACION", idCarrera).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarEstado(Integer idPostulacion, String nuevoEstado, String observacion) {
        try {
            Integer resultado = postulacionRepository.actualizarPostulacion(idPostulacion, nuevoEstado, observacion);
            if (resultado == null || resultado == -1) {
                throw new RuntimeException("El SP devolvio error al actualizar el estado de la postulacion.");
            }
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Error al actualizar el estado de la postulacion en la base de datos: " + ex.getMessage(), ex);
        }

        // Para la notificacion, recuperamos el estudiante usando el id de la postulacion
        postulacionRepository.findById(idPostulacion).ifPresent(postulacion -> {
            if (postulacion.getEstudiante() != null && postulacion.getEstudiante().getUsuario() != null) {
                try {
                    String mensaje = "Tu postulacion ha cambiado de estado a: " + nuevoEstado;

                    NotificationRequest notificationRequest = NotificationRequest.builder()
                            .titulo("Actualizacion de postulacion")
                            .mensaje(mensaje)
                            .tipo("POSTULACION")
                            .idReferencia(idPostulacion)
                            .build();

                    notificacionService.enviarNotificacion(
                            postulacion.getEstudiante().getUsuario().getIdUsuario(),
                            notificationRequest);
                } catch (Exception notifEx) {
                    System.err.println("[NOTIFICACION] No se pudo enviar notificacion (no critico): " + notifEx.getMessage());
                }
            }
        });
    }

    private PostulacionResponseDTO mapearADTO(Postulacion entidad) {
        String nombreEstudiante = "";
        String matricula = "";
        if (entidad.getEstudiante() != null && entidad.getEstudiante().getUsuario() != null) {
            nombreEstudiante = entidad.getEstudiante().getUsuario().getNombres() + " " +
                    entidad.getEstudiante().getUsuario().getApellidos();
            matricula = entidad.getEstudiante().getMatricula();
        }

        return PostulacionResponseDTO.builder()
                .idPostulacion(entidad.getIdPostulacion())
                .idConvocatoria(entidad.getConvocatoria().getIdConvocatoria())
                .asignaturaConvocatoria(entidad.getConvocatoria().getAsignatura().getNombreAsignatura())
                .idEstudiante(entidad.getEstudiante().getIdEstudiante())
                .nombreCompletoEstudiante(nombreEstudiante)
                .matriculaEstudiante(matricula)
                .fechaPostulacion(entidad.getFechaPostulacion())
                .estadoPostulacion(entidad.getEstadoPostulacion())
                .observaciones(entidad.getObservaciones())
                .activo(entidad.getActivo())
                .comisionAsignada(
                        entidad.getEvaluacionesOposicion() != null && !entidad.getEvaluacionesOposicion().isEmpty())
                .build();
    }

    private PostulacionResponseDTO mapearDesdeObjectArray(Object[] obj) {
        return PostulacionResponseDTO.builder()
                .idPostulacion((Integer) obj[0])
                .idConvocatoria((Integer) obj[1])
                .estadoPostulacion((String) obj[3])
                .build();
    }

    @Override
    @Transactional
    public String registrarPostulacionCompleta(PostulacionRequestDTO request, List<MultipartFile> archivos,
                                               List<Integer> tiposRequisito) {
        try {
            Estudiante estudiante = estudianteRepository.findByUsuario_IdUsuario(request.getIdEstudiante())
                    .orElseThrow(() -> new RuntimeException(
                            "Estudiante no encontrado para el usuario con ID: " + request.getIdEstudiante()));
            long postulacionesActivas = postulacionRepository
                    .contarPostulacionesActivasPorEstudiante(estudiante.getIdEstudiante());
            if (postulacionesActivas > 0) {
                throw new RuntimeException(
                        "Ya tienes una postulacion activa. No puedes postularte a mas de una convocatoria a la vez.");
            }

            Integer idPostulacion = postulacionRepository.crearPostulacion(
                    request.getIdConvocatoria(),
                    estudiante.getIdEstudiante(),
                    new Date(System.currentTimeMillis()),
                    "PENDIENTE",
                    request.getObservaciones());
            if (idPostulacion == null || idPostulacion == -1)
                throw new RuntimeException("Error al crear la postulacion; el SP devolvio -1");

            for (int i = 0; i < archivos.size(); i++) {
                MultipartFile archivo = archivos.get(i);
                Integer idTipoReq = tiposRequisito.get(i);

                if (!archivo.isEmpty()) {
                    try {
                        Integer idRequisito = postulacionRepository.crearRequisitoAdjunto(
                                idPostulacion,
                                idTipoReq,
                                1, // idTipoEstadoRequisito = 1 (pendiente/entregado)
                                archivo.getBytes(),
                                archivo.getOriginalFilename(),
                                new Date(System.currentTimeMillis()));

                        if (idRequisito == null || idRequisito == -1) {
                            throw new RuntimeException("El SP sp_crear_requisito_adjunto devolvio " + idRequisito
                                    + " para archivo: " + archivo.getOriginalFilename());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename(), ex);
                    }
                }
            }

            return "Postulacion registrada con exito. ID: " + idPostulacion;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en el proceso: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePostulacion(Integer idUsuario, Integer idConvocatoria) {
        // El idUsuario viene del JWT; hay que resolver al id_estudiante real
        return estudianteRepository.findByUsuario_IdUsuario(idUsuario)
                .map(est -> postulacionRepository.existePostulacionActiva(est.getIdEstudiante(), idConvocatoria))
                .orElse(false);
    }
}
