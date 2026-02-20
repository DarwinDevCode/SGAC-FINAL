package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaRequestDTO {
    @NotNull(message = "La carrera es obligatoria")
    private Integer idCarrera;

    @NotBlank(message = "El nombre de la asignatura es obligatorio")
    private String nombreAsignatura;

    @NotNull(message = "El semestre es obligatorio")
    @Min(value = 1, message = "El semestre debe ser mayor a 0")
    private Integer semestre;


}