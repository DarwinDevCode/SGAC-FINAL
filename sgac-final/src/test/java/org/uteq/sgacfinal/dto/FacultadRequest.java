package org.uteq.sgacfinal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultadRequest {
    @NotBlank(message = "El nombre de la facultad es requerido")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombreFacultad;
}
