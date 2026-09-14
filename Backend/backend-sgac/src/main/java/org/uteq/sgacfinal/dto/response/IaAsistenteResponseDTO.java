package org.uteq.sgacfinal.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IaAsistenteResponseDTO {
    private String respuesta;
    private Integer tokensUsados;
}
