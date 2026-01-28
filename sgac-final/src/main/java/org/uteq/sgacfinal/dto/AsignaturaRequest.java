package org.uteq.sgacfinal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaRequest {
    @NotNull(message = "El id de la carrera es requerido")
    private Integer idCarrera;

    @NotBlank(message = "El nombre de la asignatura es requerido")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombreAsignatura;

    @NotNull(message = "El semestre es requerido")
    @Min(value = 1, message = "El semestre debe ser al menos 1")
    private Integer semestre;
}
