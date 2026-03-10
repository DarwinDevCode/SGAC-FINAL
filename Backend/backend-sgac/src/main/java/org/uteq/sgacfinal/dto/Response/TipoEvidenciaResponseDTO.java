package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoEvidenciaResponseDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("extension_permitida")
    private String extensionPermitida;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("activo")
    private Boolean activo;
}

