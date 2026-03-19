package org.uteq.sgacfinal.dto.response.convocatorias;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDate;
import java.time.LocalTime;

public record EvaluacionOposicionResponseDTO(
        @JsonAlias("tema_exposicion") String temaExposicion,
        String lugar,
        @JsonAlias("fecha_evaluacion") LocalDate fechaEvaluacion,
        @JsonAlias("hora_inicio") LocalTime horaInicio,
        @JsonAlias("hora_fin") LocalTime horaFin,
        @JsonAlias("orden_exposicion") Integer ordenExposicion
) {}