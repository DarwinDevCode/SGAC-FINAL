package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecanoReporteCarreraDTO {
    private Integer idCarrera;
    private String nombreCarrera;
    private long totalAsignaturas;
    private long totalConvocatorias;
    private long totalPostulantes;
}
