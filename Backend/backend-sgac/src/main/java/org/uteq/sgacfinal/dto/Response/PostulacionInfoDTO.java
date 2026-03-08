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
public class PostulacionInfoDTO {

    @JsonProperty("id_postulacion")
    private Integer idPostulacion;

    @JsonProperty("fecha_postulacion")
    private LocalDate fechaPostulacion;

    @JsonProperty("estado_postulacion")
    private String estadoPostulacion;

    private String observaciones;
}