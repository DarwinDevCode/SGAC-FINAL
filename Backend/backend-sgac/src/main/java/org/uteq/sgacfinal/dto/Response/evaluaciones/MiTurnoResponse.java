package org.uteq.sgacfinal.dto.Response.evaluaciones;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MiTurnoResponse(
    Integer idEvaluacionOposicion,
    Integer orden,
    String  tema,
    String  fecha,
    String  horaInicio,
    String  horaFin,
    String  horaInicioReal,
    String  lugar,
    String  estado,
    String  nombreEstado,
    Double  puntajeFinal,
    Instant serverTime
) {}