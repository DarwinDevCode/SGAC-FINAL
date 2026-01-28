package com.sgac.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecanoRequest {
    @NotNull(message = "El id del usuario es requerido")
    private Integer idUsuario;

    @NotNull(message = "El id de la facultad es requerido")
    private Integer idFacultad;

    @NotNull(message = "La fecha de inicio de gestión es requerida")
    private LocalDate fechaInicioGestion;

    private LocalDate fechaFinGestion;

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;
}
