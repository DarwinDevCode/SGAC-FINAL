package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinadorConvocatoriaReporteDTO {
    private Integer idConvocatoria;
    private String nombreAsignatura;
    private String nombreCarrera;
    private String nombrePeriodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cuposAprobados;
    private String estado;
    private Long numeroPostulantes;
}
