package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO para la respuesta de cambio de estado a EN_REVISION
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoRevisionResponseDTO {

    @JsonProperty("exito")
    private Boolean exito;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("estado_anterior")
    private String estadoAnterior;

    @JsonProperty("estado_actual")
    private String estadoActual;

    @JsonProperty("cambio_realizado")
    private Boolean cambioRealizado;
}

