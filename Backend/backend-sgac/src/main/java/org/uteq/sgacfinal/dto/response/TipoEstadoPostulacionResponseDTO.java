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
public class TipoEstadoPostulacionResponseDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("fecha_creacion")
    private String fechaCreacion;
}

