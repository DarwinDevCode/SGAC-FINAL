package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvocatoriaReporteDTO {
    private Integer idConvocatoria;
    private String nombreAsignatura;
    private String nombreCarrera;
    private String nombreCoordinador;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado; // "ACTIVO" o "INACTIVO" según el booleano
    private Long numeroPostulantes;
}
