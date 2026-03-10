package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Request.PostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.entity.Estudiante;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.EstudianteRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.IPostulacionService;


import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostulacionServiceImpl implements IPostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final INotificacionService notificacionService;
    private final ObjectMapper objectMapper;

    @Override
    public PostulacionResponseDTO crear(PostulacionRequestDTO request) {

        return null;
    }

    @Override
    public PostulacionResponseDTO actualizar(Integer id, PostulacionRequestDTO request) {

        return null;
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = postulacionRepository.desactivarPostulacion(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al anular la postulación.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PostulacionResponseDTO buscarPorId(Integer id) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
        return mapearADTO(postulacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorEstudiante(Integer idUsuario) {
        Optional<Estudiante> estudianteOpt = estudianteRepository.findByUsuario_IdUsuario(idUsuario);
        if (estudianteOpt.isEmpty()) {
            return List.of();
        }

        List<Object[]> resultados = postulacionRepository.listarPostulacionesPorEstudianteSP(estudianteOpt.get().getIdEstudiante());

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
        Postulacion postulacion = postulacionRepository.findById(idPostulacion)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        postulacion.setObservaciones(observacion);
        
        try {
            System.out.println("Actualizando postulación ID: " + idPostulacion + " a estado: " + nuevoEstado);
            postulacionRepository.save(postulacion);
        } catch (Exception ex) {
            System.err.println("Error al guardar postulación: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error al actualizar el estado de la postulación en la base de datos: " + ex.getMessage(), ex);
        }

        if (postulacion.getEstudiante() != null && postulacion.getEstudiante().getUsuario() != null) {
            String mensaje = "Tu postulación ha cambiado de estado a: " + nuevoEstado;


        NotificationRequest notificationRequest = new NotificationRequest();


            notificacionService.enviarNotificacion(
                    postulacion.getEstudiante().getUsuario().getIdUsuario(),
                    notificationRequest
            );
        }
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
                .observaciones(entidad.getObservaciones())
                .activo(entidad.getActivo())
                .comisionAsignada(entidad.getEvaluacionesOposicion() != null && !entidad.getEvaluacionesOposicion().isEmpty())
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
    public String registrarPostulacionCompleta(PostulacionRequestDTO request, List<MultipartFile> archivos, List<Integer> tiposRequisito) {
        try {
            Estudiante estudiante = estudianteRepository.findByUsuario_IdUsuario(request.getIdEstudiante())
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado para el usuario con ID: " + request.getIdEstudiante()));
            long postulacionesActivas = postulacionRepository.contarPostulacionesActivasPorEstudiante(estudiante.getIdEstudiante());
            if (postulacionesActivas > 0) {
                throw new RuntimeException("Ya tienes una postulación activa. No puedes postularte a más de una convocatoria a la vez.");
            }

            Integer idPostulacion = postulacionRepository.crearPostulacion(
                    request.getIdConvocatoria(),
                    estudiante.getIdEstudiante(),
                    new java.sql.Date(System.currentTimeMillis()),
                    "PENDIENTE",
                    request.getObservaciones()
            );
            if (idPostulacion == null || idPostulacion == -1)
                throw new RuntimeException("Error al crear la postulación — el SP devolvió -1");

            for (int i = 0; i < archivos.size(); i++) {
                MultipartFile archivo = archivos.get(i);
                Integer idTipoReq = tiposRequisito.get(i);

                if (!archivo.isEmpty()) {
                    try {
                        System.out.println("Intentando guardar archivo: " + archivo.getOriginalFilename() + " para req ID: " + idTipoReq);
                        Integer idRequisito = postulacionRepository.crearRequisitoAdjunto(
                                idPostulacion,
                                idTipoReq,
                                1, // idTipoEstadoRequisito = 1 (PENDIENTE/ENTREGADO)
                                archivo.getBytes(),
                                archivo.getOriginalFilename(),
                                new java.sql.Date(System.currentTimeMillis()));

                        System.out.println("Resultado sp_crear_requisito_adjunto: " + idRequisito);
                        if (idRequisito == null || idRequisito == -1) {
                            throw new RuntimeException("El SP sp_crear_requisito_adjunto devolvió " + idRequisito + " para archivo: " + archivo.getOriginalFilename());
                        }
                    } catch (Exception ex) {
                        System.err.println("Error interno al guardar archivo " + archivo.getOriginalFilename() + ": " + ex.getMessage());
                        ex.printStackTrace();
                        throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename(), ex);
                    }
                }
            }

            return "Postulación registrada con éxito. ID: " + idPostulacion;

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

    @Override
    @Transactional(readOnly = true)
    public DetallePostulacionResponseDTO obtenerMiPostulacionActiva(Integer idUsuario) {
        log.info("Consultando postulación activa para usuario: {}", idUsuario);

        try {
            String jsonResultado = postulacionRepository.obtenerDetallePostulacion(idUsuario);

            if (jsonResultado == null || jsonResultado.isEmpty()) {
                return DetallePostulacionResponseDTO.builder()
                        .exito(false)
                        .codigo("ERROR_CONSULTA")
                        .mensaje("No se pudo obtener información de la postulación")
                        .build();
            }

            DetallePostulacionResponseDTO response = parsearRespuestaPostulacion(jsonResultado);

            // Si la respuesta es exitosa y tiene convocatoria, verificar si es periodo de subsanación
            if (response.getExito() != null && response.getExito()
                && response.getConvocatoria() != null
                && response.getConvocatoria().getIdConvocatoria() != null) {

                try {
                    Boolean esPeriodoSubsanacion = postulacionRepository.esPeriodoSubsanacion(
                        response.getConvocatoria().getIdConvocatoria()
                    );
                    response.setEsPeriodoSubsanacion(esPeriodoSubsanacion != null ? esPeriodoSubsanacion : false);
                } catch (Exception e) {
                    log.warn("No se pudo verificar periodo de subsanación: {}", e.getMessage());
                    response.setEsPeriodoSubsanacion(false);
                }
            } else {
                response.setEsPeriodoSubsanacion(false);
            }

            return response;

        } catch (Exception e) {
            log.error("Error al obtener detalle de postulación: {}", e.getMessage());

            String mensajeError = e.getMessage();
            if (mensajeError != null && mensajeError.contains("ERROR SISTEMA")) {
                return DetallePostulacionResponseDTO.builder()
                        .exito(false)
                        .codigo("ERROR_SISTEMA")
                        .mensaje(mensajeError)
                        .esPeriodoSubsanacion(false)
                        .build();
            }

            return DetallePostulacionResponseDTO.builder()
                    .exito(false)
                    .codigo("ERROR")
                    .mensaje("Error al consultar la postulación: " + mensajeError)
                    .esPeriodoSubsanacion(false)
                    .build();
        }
    }

    /**
     * Parsea la respuesta JSON de la función PostgreSQL fn_ver_detalle_postulacion.
     * La función retorna un JSONB estructurado con:
     * - exito, mensaje, codigo (para errores)
     * - postulacion: {id_postulacion, fecha_postulacion, estado_codigo, estado_nombre, observaciones}
     * - convocatoria: {id_convocatoria, nombre_asignatura, semestre_asignatura, nombre_carrera, nombre_docente, cupos_disponibles}
     * - cronograma: Array de {fase, codigo, inicio, fin, estado}
     * - documentos: Array de documentos con plazos de 24h
     * - resumen_documentos: {pendientes, aprobados, observados, rechazados, corregidos}
     * - es_periodo_subsanacion: Boolean
     * - es_postulacion_rechazada: Boolean
     */
    private DetallePostulacionResponseDTO parsearRespuestaPostulacion(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            Boolean exito = root.path("exito").asBoolean(false);

            if (!exito) {
                return DetallePostulacionResponseDTO.builder()
                        .exito(false)
                        .codigo(root.path("codigo").asText(""))
                        .mensaje(root.path("mensaje").asText("Error desconocido"))
                        .build();
            }

            // Parsear postulación
            PostulacionInfoDTO postulacion = null;
            JsonNode postulacionNode = root.path("postulacion");
            if (!postulacionNode.isMissingNode()) {
                postulacion = objectMapper.treeToValue(postulacionNode, PostulacionInfoDTO.class);
            }

            // Parsear convocatoria
            ConvocatoriaPostulacionDTO convocatoria = null;
            JsonNode convocatoriaNode = root.path("convocatoria");
            if (!convocatoriaNode.isMissingNode()) {
                convocatoria = objectMapper.treeToValue(convocatoriaNode, ConvocatoriaPostulacionDTO.class);
            }

            // Parsear cronograma (nuevo formato: fase, codigo, inicio, fin, estado)
            List<EtapaCronogramaDTO> cronograma = Collections.emptyList();
            JsonNode cronogramaNode = root.path("cronograma");
            if (cronogramaNode.isArray()) {
                cronograma = objectMapper.readValue(
                        cronogramaNode.toString(),
                        new TypeReference<List<EtapaCronogramaDTO>>() {}
                );
            }

            // Parsear documentos (nuevo en V47)
            List<DocumentoPostulacionDTO> documentos = Collections.emptyList();
            JsonNode documentosNode = root.path("documentos");
            if (documentosNode.isArray()) {
                documentos = objectMapper.readValue(
                        documentosNode.toString(),
                        new TypeReference<List<DocumentoPostulacionDTO>>() {}
                );
            }

            // Parsear resumen de documentos (ahora incluye 'corregidos')
            ResumenDocumentosDTO resumen = null;
            JsonNode resumenNode = root.path("resumen_documentos");
            if (!resumenNode.isMissingNode()) {
                resumen = objectMapper.treeToValue(resumenNode, ResumenDocumentosDTO.class);
            }

            // Obtener indicadores booleanos
            Boolean esPeriodoSubsanacion = root.path("es_periodo_subsanacion").asBoolean(false);
            Boolean esPostulacionRechazada = root.path("es_postulacion_rechazada").asBoolean(false);

            return DetallePostulacionResponseDTO.builder()
                    .exito(true)
                    .mensaje(root.path("mensaje").asText(""))
                    .postulacion(postulacion)
                    .convocatoria(convocatoria)
                    .cronograma(cronograma)
                    .documentos(documentos)
                    .resumenDocumentos(resumen)
                    .esPeriodoSubsanacion(esPeriodoSubsanacion)
                    .esPostulacionRechazada(esPostulacionRechazada)
                    .build();

        } catch (Exception e) {
            log.error("Error al parsear respuesta JSON de postulación: {}", e.getMessage());
            return DetallePostulacionResponseDTO.builder()
                    .exito(false)
                    .codigo("ERROR_PARSEO")
                    .mensaje("Error al procesar la respuesta: " + e.getMessage())
                    .build();
        }
    }


}