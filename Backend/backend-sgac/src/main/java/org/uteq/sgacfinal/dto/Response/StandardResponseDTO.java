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
public class StandardResponseDTO<T> {

    @JsonProperty("exito")
    private Boolean exito;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("datos")
    private T datos;
}

