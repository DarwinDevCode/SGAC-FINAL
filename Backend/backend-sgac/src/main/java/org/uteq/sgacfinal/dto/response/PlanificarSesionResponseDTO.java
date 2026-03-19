package org.uteq.sgacfinal.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanificarSesionResponseDTO {
    private boolean exito;
    private String mensaje;
    private Integer idRegistroCreado;
}