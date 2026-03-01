package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ProgresoGeneralResponse {
    private BigDecimal horasAprobadas;
    private BigDecimal horasPendientes;
    private BigDecimal horasObservadas;
    private BigDecimal horasTotalesRegistradas;
    private BigDecimal horasMaximas;
    private BigDecimal porcentajeAvance;
    private Long totalSesiones;
    private Long sesionesAprobadas;
    private Long sesionesPendientes;
    private Long sesionesObservadas;
}
