package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRequisitoPostulacionRequestDTO {
    @NotBlank
    private String nombreRequisito;
    private String descripcion;
    private Boolean activo;
}