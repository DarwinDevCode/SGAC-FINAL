package com.sgac.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRolRequest {

    @NotBlank(message = "El nombre del rol es requerido")
    @Size(max = 50, message = "El nombre del rol no puede exceder 50 caracteres")
    private String nombreTipoRol;

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;
}
