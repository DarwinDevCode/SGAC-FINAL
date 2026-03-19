package org.uteq.sgacfinal.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluarSesionResponse {
    private boolean exito;
    private String mensaje;
}