package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.dto.request.RequisitoAdjuntoRequestDTO;
import org.uteq.sgacfinal.dto.response.RequisitoAdjuntoResponseDTO;
import org.uteq.sgacfinal.dto.response.SubsanacionDocumentoResponseDTO;
import org.uteq.sgacfinal.entity.RequisitoAdjunto;
import org.uteq.sgacfinal.repository.RequisitoAdjuntoRepository;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.IRequisitoAdjuntoService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequisitoAdjuntoServiceImpl implements IRequisitoAdjuntoService {

    private final RequisitoAdjuntoRepository requisitoRepository;
    private final ObjectMapper objectMapper;
    private final INotificacionService notificacionService;

    @Override
    public RequisitoAdjuntoResponseDTO crear(RequisitoAdjuntoRequestDTO request) {
        Integer idGenerado = requisitoRepository.registrarRequisito(
                request.getIdPostulacion(),
                request.getIdTipoRequisitoPostulacion(),
                request.getIdTipoEstadoRequisito(),
                request.getArchivo(),
                request.getNombreArchivo(),
                request.getFechaSubida(),
                request.getObservacion()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al adjuntar requisito.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public RequisitoAdjuntoResponseDTO actualizar(Integer id, RequisitoAdjuntoRequestDTO request) {
        Integer resultado = requisitoRepository.actualizarRequisito(
                id,
                request.getIdTipoEstadoRequisito(),
                request.getObservacion()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar el estado del requisito.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RequisitoAdjuntoResponseDTO buscarPorId(Integer id) {
        RequisitoAdjunto requisito = requisitoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisito adjunto no encontrado con ID: " + id));
        return mapearADTO(requisito);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequisitoAdjuntoResponseDTO> listarPorPostulacion(Integer idPostulacion) {
        return requisitoRepository.findByPostulacion_IdPostulacion(idPostulacion)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private RequisitoAdjuntoResponseDTO mapearADTO(RequisitoAdjunto entidad) {
        String nombreRequisito = entidad.getTipoRequisitoPostulacion() != null
                ? entidad.getTipoRequisitoPostulacion().getNombreRequisito() : "";
        Integer idTipoReq = entidad.getTipoRequisitoPostulacion() != null
                ? entidad.getTipoRequisitoPostulacion().getIdTipoRequisitoPostulacion() : null;
        return RequisitoAdjuntoResponseDTO.builder()
                .idRequisitoAdjunto(entidad.getIdRequisitoAdjunto())
                .idPostulacion(entidad.getPostulacion().getIdPostulacion())
                .idTipoRequisitoPostulacion(idTipoReq)
                .nombreRequisito(nombreRequisito)
                .nombreEstado(entidad.getTipoEstadoRequisito().getNombreEstado())
                .nombreArchivo(entidad.getNombreArchivo())
                .fechaSubida(entidad.getFechaSubida())
                .observacion(entidad.getObservacion())
                .build();
    }

    private RequisitoAdjuntoResponseDTO mapearDesdeObjectArray(Object[] obj) {
        return RequisitoAdjuntoResponseDTO.builder()
                .idRequisitoAdjunto((Integer) obj[0])
                .nombreArchivo((String) obj[1])
                .fechaSubida(obj[2] != null ? ((Date) obj[2]).toLocalDate() : null)
                .build();
    }

    @Override
    @Transactional
    public RequisitoAdjuntoResponseDTO reemplazar(Integer idAdjunto, MultipartFile archivo) {
        try {
            byte[] bytes = archivo.getBytes();
            String nombre = archivo.getOriginalFilename();
            Integer resultado = requisitoRepository.reemplazarRequisito(idAdjunto, bytes, nombre, LocalDate.now());
            if (resultado == null || resultado == -1) {
                throw new RuntimeException("Error al reemplazar el documento adjunto.");
            }
            return buscarPorId(idAdjunto);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SubsanacionDocumentoResponseDTO subsanarDocumentoObservado(Integer idUsuario, Integer idRequisitoAdjunto, MultipartFile archivo) {
        log.info("Subsanando documento {} para usuario {}", idRequisitoAdjunto, idUsuario);

        try {
            byte[] bytes = archivo.getBytes();
            String nombreArchivo = archivo.getOriginalFilename();

            // Llamar a la función PostgreSQL
            String jsonResultado = requisitoRepository.subsanarDocumentoObservado(
                    idUsuario,
                    idRequisitoAdjunto,
                    bytes,
                    nombreArchivo
            );

            if (jsonResultado == null || jsonResultado.isEmpty()) {
                return SubsanacionDocumentoResponseDTO.error(
                        "ERROR_CONSULTA",
                        "No se pudo procesar la subsanación del documento"
                );
            }

            // Parsear respuesta JSON
            SubsanacionDocumentoResponseDTO response = parsearRespuestaSubsanacion(jsonResultado);

            // Si fue exitoso, enviar notificación en tiempo real por WebSocket al coordinador
            if (response.getExito()) {
                enviarNotificacionWebSocketCoordinador(jsonResultado);
            }

            return response;

        } catch (Exception e) {
            log.error("Error al subsanar documento: {}", e.getMessage());

            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("ERROR SISTEMA")) {
                return SubsanacionDocumentoResponseDTO.error("ERROR_SISTEMA", mensaje);
            }

            return SubsanacionDocumentoResponseDTO.error(
                    "ERROR",
                    "Error al procesar el archivo: " + mensaje
            );
        }
    }

    /**
     * Envía notificación en tiempo real por WebSocket al coordinador.
     */
    private void enviarNotificacionWebSocketCoordinador(String jsonResultado) {
        try {
            JsonNode root = objectMapper.readTree(jsonResultado);

            // Obtener el ID del coordinador desde la respuesta de PostgreSQL
            Integer idCoordinador = root.path("id_coordinador").asInt(0);
            String nombreEstudiante = root.path("nombre_estudiante").asText("");
            String nombreRequisito = root.path("nombre_requisito").asText("");
            Integer idPostulacion = root.path("id_postulacion").asInt(0);

            if (idCoordinador > 0) {
                NotificationRequest notifRequest = new NotificationRequest();
                notifRequest.setTitulo("Documento Subsanado");
                notifRequest.setMensaje("El estudiante " + nombreEstudiante +
                        " ha corregido el documento '" + nombreRequisito +
                        "'. Pendiente de nueva revisión.");
                notifRequest.setTipo("SUBSANACION_DOCUMENTO");
                notifRequest.setIdReferencia(idPostulacion);

                notificacionService.enviarNotificacion(idCoordinador, notifRequest);
                log.info("Notificación WebSocket enviada al coordinador ID: {}", idCoordinador);
            }
        } catch (Exception e) {
            // No fallar la operación principal si falla el envío de notificación
            log.warn("No se pudo enviar notificación WebSocket al coordinador: {}", e.getMessage());
        }
    }

    /**
     * Parsea la respuesta JSON de la función fn_subsanar_documento_estudiante.
     */
    private SubsanacionDocumentoResponseDTO parsearRespuestaSubsanacion(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            Boolean exito = root.path("exito").asBoolean(false);
            String codigo = root.path("codigo").asText("");
            String mensaje = root.path("mensaje").asText("");

            if (!exito) {
                return SubsanacionDocumentoResponseDTO.builder()
                        .exito(false)
                        .codigo(codigo)
                        .mensaje(mensaje)
                        .build();
            }

            return SubsanacionDocumentoResponseDTO.builder()
                    .exito(true)
                    .codigo(codigo)
                    .mensaje(mensaje)
                    .idRequisitoAdjunto(root.path("id_requisito_adjunto").asInt())
                    .nuevoEstado(root.path("nuevo_estado").asText("CORREGIDO"))
                    .notificacionEnviada(root.path("notificacion_enviada").asBoolean(false))
                    .build();

        } catch (Exception e) {
            log.error("Error al parsear respuesta de subsanación: {}", e.getMessage());
            return SubsanacionDocumentoResponseDTO.error(
                    "ERROR_PARSEO",
                    "Error al procesar la respuesta: " + e.getMessage()
            );
        }
    }
}
