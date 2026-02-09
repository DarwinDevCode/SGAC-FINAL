package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionMeritosResponseDTO {
    private Integer idEvaluacionMeritos;
    private Integer idPostulacion;
    private BigDecimal notaAsignatura;
    private BigDecimal notaSemestres;
    private BigDecimal notaEventos;
    private BigDecimal notaExperiencia;
    private LocalDate fechaEvaluacion;
}