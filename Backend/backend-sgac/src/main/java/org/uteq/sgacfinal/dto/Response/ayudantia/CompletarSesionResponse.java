package org.uteq.sgacfinal.dto.Response.ayudantia;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompletarSesionResponse {
    private boolean exito;
    private String mensaje;
}