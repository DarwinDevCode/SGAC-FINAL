package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadoEvidenciaAyudantiaRequestDTO {
    @NotBlank
    private String nombreEstado;
    private String descripcion;
}