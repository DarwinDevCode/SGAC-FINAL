package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el resumen de estados de los documentos de una postulación.
 * Mapea el objeto resumen_documentos de fn_ver_detalle_postulacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumenDocumentosDTO {

    private Integer pendientes;
    private Integer aprobados;
    private Integer observados;
    private Integer rechazados;
    private Integer corregidos;
}