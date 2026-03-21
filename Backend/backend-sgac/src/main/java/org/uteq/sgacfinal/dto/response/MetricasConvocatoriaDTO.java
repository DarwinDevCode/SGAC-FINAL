package org.uteq.sgacfinal.dto.response;

import lombok.*;

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
