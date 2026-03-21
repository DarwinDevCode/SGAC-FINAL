package org.uteq.sgacfinal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta completa del detalle de postulación activa.
 * Mapea el JSONB retornado por fn_ver_detalle_postulacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePostulacionResponseDTO {

    private Boolean exito;
    private String codigo;
    private String mensaje;

    /** Información básica de la postulación */
    private PostulacionInfoDTO postulacion;

    /** Información de la convocatoria asociada */
    private ConvocatoriaPostulacionDTO convocatoria;

    /** Cronograma dinámico de fases del periodo académico */
    private List<EtapaCronogramaDTO> cronograma;

    /** Resumen de conteo de estados de documentos */
    @JsonProperty("resumen_documentos")
    private ResumenDocumentosDTO resumenDocumentos;

    /**
     * Indica si actualmente es posible subsanar/reemplazar documentos observados.
     * TRUE si estamos en las fases POSTULACION o EVALUACION_REQUISITOS del periodo académico.
     * Se obtiene llamando a fn_es_periodo_subsanacion(id_convocatoria).
     */
    @JsonProperty("es_periodo_subsanacion")
    private Boolean esPeriodoSubsanacion;

    /**
     * Indica si la postulación ha sido rechazada definitivamente.
     * Cuando es TRUE, todas las acciones sobre la postulación deben estar bloqueadas.
     */
    @JsonProperty("es_postulacion_rechazada")
    private Boolean esPostulacionRechazada;

    /**
     * Lista de documentos adjuntos con información de estado y plazos de subsanación.
     */
    private List<DocumentoPostulacionDTO> documentos;
}