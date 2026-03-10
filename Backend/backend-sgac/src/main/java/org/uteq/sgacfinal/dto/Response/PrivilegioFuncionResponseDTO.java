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
public class PrivilegioFuncionResponseDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("nombre_privilegio")
    private String nombrePrivilegio;

    @JsonProperty("codigo_interno")
    private String codigoInterno;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("activo")
    private Boolean activo;
}

