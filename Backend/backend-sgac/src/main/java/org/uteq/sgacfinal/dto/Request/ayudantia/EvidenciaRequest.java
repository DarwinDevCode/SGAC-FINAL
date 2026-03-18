package org.uteq.sgacfinal.dto.Request.ayudantia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvidenciaRequest {
    @NotNull(message = "El tipo de evidencia es obligatorio.")
    private Integer idTipoEvidencia;

    @NotBlank(message = "El nombre del archivo referenciado es obligatorio.")
    private String nombreArchivoReferencia;
}