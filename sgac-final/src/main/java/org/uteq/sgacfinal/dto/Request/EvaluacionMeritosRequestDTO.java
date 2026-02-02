package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionMeritosRequestDTO {
    @NotNull
    private Integer idPostulacion;
    private BigDecimal notaAsignatura;
    private BigDecimal notaSemestres;
    private BigDecimal notaEventos;
    private BigDecimal notaExperiencia;
    private LocalDate fechaEvaluacion;
}