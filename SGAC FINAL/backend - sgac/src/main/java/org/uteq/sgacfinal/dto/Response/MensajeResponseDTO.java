package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MensajeResponseDTO {
    private String mensaje;
    private boolean exito;
}
