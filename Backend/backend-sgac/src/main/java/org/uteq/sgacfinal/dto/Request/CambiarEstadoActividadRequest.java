package org.uteq.sgacfinal.dto.Request;

import lombok.Data;

@Data
public class CambiarEstadoActividadRequest {
    private String estado;          // PENDIENTE | ACEPTADO | RECHAZADO | OBSERVADO
    private String observaciones;
}
