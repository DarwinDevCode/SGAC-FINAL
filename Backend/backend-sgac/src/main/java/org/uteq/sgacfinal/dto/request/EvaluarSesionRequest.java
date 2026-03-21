package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EvaluarSesionRequest {

    @NotBlank(message = "El código del estado es obligatorio.")
    @Pattern(regexp = "^(APROBADO|RECHAZADO)$", message = "El estado solo puede ser APROBADO o RECHAZADO.")
    private String codigoEstado;

    private String observaciones;
}