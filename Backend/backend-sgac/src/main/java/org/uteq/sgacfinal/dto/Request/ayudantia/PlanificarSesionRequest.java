package org.uteq.sgacfinal.dto.Request.ayudantia;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PlanificarSesionRequest {
    @NotNull(message = "El ID de la ayudantía es obligatorio.")
    private Integer idAyudantia;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha de planificación no puede ser en el pasado.")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria.")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria.")
    private LocalTime horaFin;

    @NotBlank(message = "El lugar o enlace de la sesión es obligatorio.")
    private String lugar;

    @NotBlank(message = "El tema a tratar es obligatorio.")
    private String temaTratado;
}