package org.uteq.sgacfinal.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentoPostulacionDTO {

    @JsonProperty("id_requisito_adjunto")
    private Integer idRequisitoAdjunto;

    @JsonProperty("id_tipo_requisito")
    private Integer idTipoRequisito;

    /** Nombre del tipo de requisito (de fn_ver_detalle_postulacion) */
    @JsonProperty("tipo_requisito")
    private String tipoRequisito;

    @JsonProperty("nombre_requisito")
    private String nombreRequisito;

    @JsonProperty("descripcion_requisito")
    private String descripcionRequisito;

    @JsonProperty("tipo_documento_permitido")
    private String tipoDocumentoPermitido;

    @JsonProperty("nombre_archivo")
    private String nombreArchivo;

    @JsonProperty("fecha_subida")
    private LocalDate fechaSubida;

    private String estado;

    /** Nombre del estado del documento (de fn_ver_detalle_postulacion) */
    @JsonProperty("estado_nombre")
    private String estadoNombre;

    @JsonProperty("id_tipo_estado_requisito")
    private Integer idTipoEstadoRequisito;

    private String observacion;

    @JsonProperty("es_editable")
    private Boolean esEditable;

    @JsonProperty("tiene_archivo")
    private Boolean tieneArchivo;

    // === NUEVOS CAMPOS PARA GESTIÓN DE VENTANA 24 HORAS ===

    /**
     * Timestamp del momento en que el coordinador marcó el documento como OBSERVADO.
     * Usado para calcular la ventana de 24h de subsanación.
     */
    @JsonProperty("fecha_observacion")
    private LocalDateTime fechaObservacion;

    /**
     * Fecha y hora límite para subsanar el documento (fecha_observacion + 24 horas).
     * Solo tiene valor cuando el estado es OBSERVADO.
     */
    @JsonProperty("fecha_limite_subsanacion")
    private String fechaLimiteSubsanacion;

    /**
     * Tiempo restante en segundos para subsanar el documento.
     * Solo tiene valor cuando el estado es OBSERVADO.
     */
    @JsonProperty("tiempo_restante_segundos")
    private Integer tiempoRestanteSegundos;

    /**
     * Indica si el plazo de 24 horas ya expiró.
     * TRUE si la ventana de subsanación ha cerrado.
     */
    @JsonProperty("plazo_expirado")
    private Boolean plazoExpirado;
}