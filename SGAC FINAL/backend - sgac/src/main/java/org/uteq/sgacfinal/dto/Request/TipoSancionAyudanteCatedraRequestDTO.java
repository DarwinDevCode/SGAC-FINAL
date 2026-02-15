package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSancionAyudanteCatedraRequestDTO {
    @NotBlank
    private String nombreTipoSancion;
    private Boolean activo;
}