package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteAsignaturaRequestDTO {
    @NotNull
    private Integer idDocente;
    @NotNull
    private Integer idAsignatura;
    private Boolean activo;
}