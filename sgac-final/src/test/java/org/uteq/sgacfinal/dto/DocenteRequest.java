package org.uteq.sgacfinal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteRequest {
    @NotNull(message = "El id del usuario es requerido")
    private Integer idUsuario;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;
}
