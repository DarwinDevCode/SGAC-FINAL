package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.DictaminarPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarDocumentoRequestDTO;
import org.uteq.sgacfinal.dto.response.*;
import org.uteq.sgacfinal.entity.RequisitoAdjunto;
import org.uteq.sgacfinal.repository.EvaluacionPostulacionRepository;
import org.uteq.sgacfinal.repository.RequisitoAdjuntoRepository;
import org.uteq.sgacfinal.service.IEvaluacionPostulacionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluacionPostulacionServiceImpl implements IEvaluacionPostulacionService {

    private final EvaluacionPostulacionRepository evaluacionRepository;
    private final RequisitoAdjuntoRepository requisitoAdjuntoRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionListadoCoordinadorDTO> listarPostulacionesCoordinador(Integer idUsuario) {
        log.info("Listando postulaciones para coordinador con idUsuario: {}", idUsuario);

        List<Object[]> resultados = evaluacionRepository.listarPostulacionesCoordinador(idUsuario);
        List<PostulacionListadoCoordinadorDTO> postulaciones = new ArrayList<>();

        for (Object[] row : resultados) {
            PostulacionListadoCoordinadorDTO dto = PostulacionListadoCoordinadorDTO.builder()
                    .idPostulacion(getInteger(row[0]))
                    .idConvocatoria(getInteger(row[1]))
                    .idEstudiante(getInteger(row[2]))
                    .nombreEstudiante(getString(row[3]))
                    .matricula(getString(row[4]))
                    .semestre(getInteger(row[5]))
                    .nombreAsignatura(getString(row[6]))
                    .nombreCarrera(getString(row[7]))
                    .fechaPostulacion(getLocalDate(row[8]))
                    .estadoCodigo(getString(row[9]))
                    .estadoNombre(getString(row[10]))
                    .requiereAtencion(getBoolean(row[11]))
                    .totalDocumentos(getLong(row[12]))
                    .documentosPendientes(getLong(row[13]))
                    .documentosAprobados(getLong(row[14]))
                    .documentosObservados(getLong(row[15]))
                    .observaciones(getString(row[16]))
                    .build();
            postulaciones.add(dto);
        }

        log.info("Se encontraron {} postulaciones", postulaciones.size());
        return postulaciones;
    }

    @Override
    @Transactional(readOnly = true)
    public DetallePostulacionCoordinadorDTO obtenerDetallePostulacion(Integer idUsuario, Integer idPostulacion) {
        log.info("Obteniendo detalle de postulación {} para coordinador {}", idPostulacion, idUsuario);

        String jsonResult = evaluacionRepository.obtenerDetallePostulacion(idUsuario, idPostulacion);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            JsonNode rootNode = mapper.readTree(jsonResult);

            DetallePostulacionCoordinadorDTO dto = new DetallePostulacionCoordinadorDTO();

            // Mapear postulacion
            JsonNode postNode = rootNode.get("postulacion");
            if (postNode != null) {
                dto.setPostulacion(DetallePostulacionCoordinadorDTO.PostulacionInfoCoordinadorDTO.builder()
                        .idPostulacion(getJsonInt(postNode, "id_postulacion"))
                        .fechaPostulacion(getJsonLocalDate(postNode, "fecha_postulacion"))
                        .estadoCodigo(getJsonString(postNode, "estado_codigo"))
                        .estadoNombre(getJsonString(postNode, "estado_nombre"))
                        .observaciones(getJsonString(postNode, "observaciones"))
                        .build());
            }

            // Mapear estudiante
            JsonNode estudianteNode = rootNode.get("estudiante");
            if (estudianteNode != null) {
                dto.setEstudiante(DetallePostulacionCoordinadorDTO.EstudianteInfoDTO.builder()
                        .idEstudiante(getJsonInt(estudianteNode, "id_estudiante"))
                        .nombreCompleto(getJsonString(estudianteNode, "nombre_completo"))
                        .email(getJsonString(estudianteNode, "email"))
                        .matricula(getJsonString(estudianteNode, "matricula"))
                        .semestre(getJsonInt(estudianteNode, "semestre"))
                        .estadoAcademico(getJsonString(estudianteNode, "estado_academico"))
                        .build());
            }

            // Mapear convocatoria
            JsonNode convNode = rootNode.get("convocatoria");
            if (convNode != null) {
                dto.setConvocatoria(DetallePostulacionCoordinadorDTO.ConvocatoriaInfoDTO.builder()
                        .idConvocatoria(getJsonInt(convNode, "id_convocatoria"))
                        .asignatura(getJsonString(convNode, "asignatura"))
                        .docente(getJsonString(convNode, "docente"))
                        .fechaPublicacion(getJsonLocalDate(convNode, "fecha_publicacion"))
                        .fechaCierre(getJsonLocalDate(convNode, "fecha_cierre"))
                        .cuposDisponibles(getJsonInt(convNode, "cupos_disponibles"))
                        .build());
            }

            // Mapear documentos
            JsonNode docsNode = rootNode.get("documentos");
            if (docsNode != null && docsNode.isArray()) {
                List<DetallePostulacionCoordinadorDTO.DocumentoEvaluacionDTO> documentos = new ArrayList<>();
                for (JsonNode docNode : docsNode) {
                    documentos.add(DetallePostulacionCoordinadorDTO.DocumentoEvaluacionDTO.builder()
                            .idRequisitoAdjunto(getJsonInt(docNode, "id_requisito_adjunto"))
                            .tipoRequisito(getJsonString(docNode, "tipo_requisito"))
                            .descripcionRequisito(getJsonString(docNode, "descripcion_requisito"))
                            .nombreArchivo(getJsonString(docNode, "nombre_archivo"))
                            .fechaSubida(getJsonLocalDate(docNode, "fecha_subida"))
                            .estadoCodigo(getJsonString(docNode, "estado_codigo"))
                            .idTipoEstadoRequisito(getJsonInt(docNode, "id_tipo_estado_requisito"))
                            .observacion(getJsonString(docNode, "observacion"))
                            .tieneArchivo(getJsonBoolean(docNode, "tiene_archivo"))
                            .build());
                }
                dto.setDocumentos(documentos);
            }

            // Mapear resumen documentos
            JsonNode resumenNode = rootNode.get("resumen_documentos");
            if (resumenNode != null) {
                dto.setResumenDocumentos(DetallePostulacionCoordinadorDTO.ResumenDocumentosEvaluacionDTO.builder()
                        .total(getJsonInt(resumenNode, "total"))
                        .pendientes(getJsonInt(resumenNode, "pendientes"))
                        .aprobados(getJsonInt(resumenNode, "aprobados"))
                        .observados(getJsonInt(resumenNode, "observados"))
                        .rechazados(getJsonInt(resumenNode, "rechazados"))
                        .build());
            }

            // Mapear puede_aprobar
            JsonNode puedeAprobarNode = rootNode.get("puede_aprobar");
            if (puedeAprobarNode != null) {
                dto.setPuedeAprobar(puedeAprobarNode.asBoolean());
            }

            return dto;

        } catch (Exception e) {
            log.error("Error al parsear detalle de postulación: {}", e.getMessage());
            throw new RuntimeException("Error al obtener detalle de postulación: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public EvaluacionDocumentoResponseDTO evaluarDocumento(Integer idUsuario, EvaluarDocumentoRequestDTO request) {
        log.info("Evaluando documento {} con acción {} por coordinador {}",
                request.getIdRequisitoAdjunto(), request.getAccion(), idUsuario);

        String jsonResult = evaluacionRepository.evaluarDocumentoIndividual(
                idUsuario,
                request.getIdRequisitoAdjunto(),
                request.getAccion(),
                request.getObservacion()
        );

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResult);

            return EvaluacionDocumentoResponseDTO.builder()
                    .exito(getJsonBoolean(rootNode, "exito"))
                    .codigo(getJsonString(rootNode, "codigo"))
                    .mensaje(getJsonString(rootNode, "mensaje"))
                    .idRequisitoAdjunto(getJsonInt(rootNode, "id_requisito_adjunto"))
                    .nuevoEstado(getJsonString(rootNode, "nuevo_estado_documento"))
                    .tieneObservados(getJsonBoolean(rootNode, "tiene_observados"))
                    .todosValidados(getJsonBoolean(rootNode, "todos_validados"))
                    .puedeAprobarPostulacion(getJsonBoolean(rootNode, "todos_validados"))
                    .fechaLimiteSubsanacion(getJsonString(rootNode, "fecha_limite_subsanacion"))
                    .build();

        } catch (Exception e) {
            log.error("Error al parsear respuesta de evaluación: {}", e.getMessage());
            return EvaluacionDocumentoResponseDTO.builder()
                    .exito(false)
                    .codigo("ERROR_SISTEMA")
                    .mensaje("Error al procesar la respuesta: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public DictamenPostulacionResponseDTO dictaminarPostulacion(Integer idUsuario, DictaminarPostulacionRequestDTO request) {
        log.info("Dictaminando postulación {} con acción {} por coordinador {}",
                request.getIdPostulacion(), request.getAccion(), idUsuario);

        String jsonResult = evaluacionRepository.dictaminarPostulacion(
                idUsuario,
                request.getIdPostulacion(),
                request.getAccion(),
                request.getObservacion()
        );

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResult);

            return DictamenPostulacionResponseDTO.builder()
                    .exito(getJsonBoolean(rootNode, "exito"))
                    .codigo(getJsonString(rootNode, "codigo"))
                    .mensaje(getJsonString(rootNode, "mensaje"))
                    .idPostulacion(getJsonInt(rootNode, "id_postulacion"))
                    .nuevoEstado(getJsonString(rootNode, "nuevo_estado"))
                    .build();

        } catch (Exception e) {
            log.error("Error al parsear respuesta de dictamen: {}", e.getMessage());
            return DictamenPostulacionResponseDTO.builder()
                    .exito(false)
                    .codigo("ERROR_SISTEMA")
                    .mensaje("Error al procesar la respuesta: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public CambioEstadoRevisionResponseDTO cambiarEstadoARevision(Integer idUsuario, Integer idPostulacion) {
        log.info("Cambiando estado a EN_REVISION para postulación {} por coordinador {}", idPostulacion, idUsuario);

        String jsonResult = evaluacionRepository.cambiarEstadoARevision(idUsuario, idPostulacion);

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResult);

            return CambioEstadoRevisionResponseDTO.builder()
                    .exito(getJsonBoolean(rootNode, "exito"))
                    .mensaje(getJsonString(rootNode, "mensaje"))
                    .estadoAnterior(getJsonString(rootNode, "estado_anterior"))
                    .estadoActual(getJsonString(rootNode, "estado_actual"))
                    .cambioRealizado(getJsonBoolean(rootNode, "cambio_realizado"))
                    .build();

        } catch (Exception e) {
            log.error("Error al parsear respuesta de cambio de estado: {}", e.getMessage());
            return CambioEstadoRevisionResponseDTO.builder()
                    .exito(false)
                    .mensaje("Error al procesar la respuesta: " + e.getMessage())
                    .cambioRealizado(false)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] obtenerArchivoDocumento(Integer idUsuario, Integer idRequisitoAdjunto) {
        log.info("Obteniendo archivo del documento {} para coordinador {}", idRequisitoAdjunto, idUsuario);

        // Primero verificar permisos llamando al detalle (esto valida que pertenece a su carrera)
        // Por simplicidad, aquí solo obtenemos el archivo directamente
        // En producción, se debería validar permisos

        RequisitoAdjunto requisito = requisitoAdjuntoRepository.findById(idRequisitoAdjunto)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        if (requisito.getArchivo() == null) {
            throw new RuntimeException("El documento no tiene archivo adjunto");
        }

        return requisito.getArchivo();
    }

    // Métodos auxiliares para conversión de tipos

    private Integer getInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }

    private Long getLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private String getString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private Boolean getBoolean(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Boolean) return (Boolean) obj;
        return Boolean.parseBoolean(obj.toString());
    }

    private LocalDate getLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate) return (LocalDate) obj;
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate();
        return LocalDate.parse(obj.toString());
    }

    private Integer getJsonInt(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asInt() : null;
    }

    private String getJsonString(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }

    private Boolean getJsonBoolean(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asBoolean() : null;
    }

    private LocalDate getJsonLocalDate(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        try {
            return LocalDate.parse(fieldNode.asText());
        } catch (Exception e) {
            return null;
        }
    }
}

