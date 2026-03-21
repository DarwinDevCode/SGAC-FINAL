package org.uteq.sgacfinal.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoEstadoEvidenciaResponseDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("nombre_estado")
    private String nombreEstado;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("activo")
    private Boolean activo;
}

