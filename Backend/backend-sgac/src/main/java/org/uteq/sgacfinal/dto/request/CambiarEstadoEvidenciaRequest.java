package org.uteq.sgacfinal.dto.request;

import lombok.Data;

@Data
public class CambiarEstadoEvidenciaRequest {
    private String estado;
    private String observaciones;
}
