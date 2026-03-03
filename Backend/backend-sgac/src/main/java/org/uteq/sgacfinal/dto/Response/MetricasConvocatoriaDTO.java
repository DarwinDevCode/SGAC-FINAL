package org.uteq.sgacfinal.dto.Response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricasConvocatoriaDTO {
    private Long totalConvocatorias;
    private Long convocatoriasActivas;
    private Long totalPostulaciones;
    private Long postulacionesPendientes;
    private Long postulacionesAprobadas;
}
