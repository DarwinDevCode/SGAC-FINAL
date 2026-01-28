package org.uteq.sgacfinal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademicoRequest {
    @NotBlank(message = "El nombre del periodo es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombrePeriodo;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate fechaFin;

    @NotBlank(message = "El estado es requerido")
    @Size(max = 30, message = "El estado no puede exceder 30 caracteres")
    private String estado;
}
