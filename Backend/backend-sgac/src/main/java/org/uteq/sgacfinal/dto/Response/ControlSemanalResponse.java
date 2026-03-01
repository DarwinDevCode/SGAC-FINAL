package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ControlSemanalResponse {
    private LocalDate semanaInicio;
    private LocalDate semanaFin;
    private BigDecimal horasRegistradas;
    private BigDecimal horasAprobadasSemana;
    private BigDecimal horasPendientesSemana;
    private BigDecimal limiteSemanal;
    private BigDecimal horasDisponibles;
    private Boolean superaLimite;
    private Long sesionesSemana;
}