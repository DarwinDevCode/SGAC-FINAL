package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteRequestDTO {
    @NotNull
    private Integer idUsuario;
    @NotNull
    private Integer idCarrera;
    @NotBlank
    private String matricula;
    @NotNull
    private Integer semestre;
    private String estadoAcademico;
}