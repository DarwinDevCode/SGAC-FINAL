package org.uteq.sgacfinal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO para la respuesta de dictamen de postulación
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictamenPostulacionResponseDTO {

    @JsonProperty("exito")
    private Boolean exito;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("id_postulacion")
    private Integer idPostulacion;

    @JsonProperty("nuevo_estado")
    private String nuevoEstado;
}

