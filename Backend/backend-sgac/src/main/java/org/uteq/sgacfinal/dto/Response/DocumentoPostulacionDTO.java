package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoPostulacionDTO {

    @JsonProperty("id_requisito_adjunto")
    private Integer idRequisitoAdjunto;

    @JsonProperty("id_tipo_requisito")
    private Integer idTipoRequisito;

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

    @JsonProperty("id_tipo_estado_requisito")
    private Integer idTipoEstadoRequisito;

    private String observacion;

    @JsonProperty("es_editable")
    private Boolean esEditable;

    @JsonProperty("tiene_archivo")
    private Boolean tieneArchivo;
}