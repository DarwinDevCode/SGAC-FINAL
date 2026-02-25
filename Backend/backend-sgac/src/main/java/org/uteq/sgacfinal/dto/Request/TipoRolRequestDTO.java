package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRolRequestDTO {
    @NotBlank
    private String nombreTipoRol;
    private Boolean activo;
}