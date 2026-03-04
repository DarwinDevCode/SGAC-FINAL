package org.uteq.sgacfinal.dto.Request;

import lombok.Data;

@Data
public class CambiarEstadoEvidenciaRequest {
    private String estado;
    private String observaciones;
}
