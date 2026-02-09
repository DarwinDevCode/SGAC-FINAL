package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionOposicionRequestDTO {
    @NotNull
    private Integer idPostulacion;
    private String temaExposicion;
    private LocalDate fechaEvaluacion;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String lugar;
    private String estado;
}