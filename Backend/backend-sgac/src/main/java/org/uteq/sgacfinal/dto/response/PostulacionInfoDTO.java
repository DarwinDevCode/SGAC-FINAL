package org.uteq.sgacfinal.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostulacionInfoDTO {

    @JsonProperty("id_postulacion")
    private Integer idPostulacion;

    @JsonProperty("fecha_postulacion")
    private LocalDate fechaPostulacion;

    @JsonProperty("estado_codigo")
    private String estadoCodigo;

    @JsonProperty("estado_nombre")
    private String estadoNombre;

    private String observaciones;
}