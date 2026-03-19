package org.uteq.sgacfinal.dto.request.ayudantia;

import java.time.LocalDate;
import java.time.LocalTime;

public record PlanificarSesionRequestDTO(
    Integer idUsuario,
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin,
    String lugar,
    String tema
) {}