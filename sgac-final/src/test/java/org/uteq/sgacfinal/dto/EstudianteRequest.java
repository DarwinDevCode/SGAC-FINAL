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
public class EstudianteRequest {
    @NotNull(message = "El id del usuario es requerido")
    private Integer idUsuario;

    @NotNull(message = "El id de la carrera es requerido")
    private Integer idCarrera;

    @NotBlank(message = "La matrícula es requerida")
    @Size(max = 30, message = "La matrícula no puede exceder 30 caracteres")
    private String matricula;

    @NotNull(message = "El semestre es requerido")
    @Min(value = 1, message = "El semestre debe ser al menos 1")
    private Integer semestre;

    @Size(max = 30, message = "El estado académico no puede exceder 30 caracteres")
    private String estadoAcademico;
}
