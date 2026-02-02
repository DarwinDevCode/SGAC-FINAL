package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionOposicionResponseDTO {
    private Integer idEvaluacionOposicion;
    private Integer idPostulacion;
    private String temaExposicion;
    private LocalDate fechaEvaluacion;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String lugar;
    private String estado;
}