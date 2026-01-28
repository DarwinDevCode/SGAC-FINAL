package org.uteq.sgacfinal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvocatoriaRequest {
    @NotNull(message = "El id del periodo académico es requerido")
    private Integer idPeriodoAcademico;

    @NotNull(message = "El id de la asignatura es requerido")
    private Integer idAsignatura;

    @NotNull(message = "El id del docente es requerido")
    private Integer idDocente;

    private Integer idPlazoActividad;

    @NotNull(message = "Los cupos disponibles son requeridos")
    @Min(value = 1, message = "Debe haber al menos 1 cupo disponible")
    private Integer cuposDisponibles;

    private LocalDate fechaPublicacion;
    private LocalDate fechaCierre;

    @Size(max = 30, message = "El estado no puede exceder 30 caracteres")
    private String estado;

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;
}
