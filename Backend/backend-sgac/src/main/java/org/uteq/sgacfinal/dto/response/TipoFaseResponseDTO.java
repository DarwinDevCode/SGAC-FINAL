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
public class TipoFaseResponseDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("orden")
    private Integer orden;

    @JsonProperty("activo")
    private Boolean activo;
}

