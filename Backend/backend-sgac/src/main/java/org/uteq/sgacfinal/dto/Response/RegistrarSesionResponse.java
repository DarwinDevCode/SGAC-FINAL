package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrarSesionResponse {
    private Boolean exito;
    private String mensaje;
    private Integer idRegistroCreado;
}