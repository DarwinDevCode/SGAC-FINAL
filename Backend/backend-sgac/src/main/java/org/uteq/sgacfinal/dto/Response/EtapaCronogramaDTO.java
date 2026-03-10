package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para las etapas del cronograma dinámico del periodo.
 * Mapea el array de objetos generado por la función fn_ver_detalle_postulacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtapaCronogramaDTO {

    /** Nombre legible de la fase (ej: "Postulación", "Revisión Requisitos") */
    private String fase;

    /** Código único de la fase (ej: "POSTULACION", "REVISION_REQUISITOS") */
    private String codigo;

    /** Fecha de inicio de la fase */
    private LocalDate inicio;

    /** Fecha de fin de la fase */
    private LocalDate fin;

    /** Estado actual de la fase: PENDIENTE, EN CURSO, FINALIZADA */
    private String estado;
}