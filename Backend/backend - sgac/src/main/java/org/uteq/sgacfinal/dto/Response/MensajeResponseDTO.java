package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MensajeResponseDTO {
    private String mensaje;
    private boolean exito;
}
