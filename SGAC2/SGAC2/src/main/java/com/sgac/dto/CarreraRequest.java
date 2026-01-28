package com.sgac.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraRequest {
    @NotNull(message = "El id de la facultad es requerido")
    private Integer idFacultad;

    @NotBlank(message = "El nombre de la carrera es requerido")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombreCarrera;
}
