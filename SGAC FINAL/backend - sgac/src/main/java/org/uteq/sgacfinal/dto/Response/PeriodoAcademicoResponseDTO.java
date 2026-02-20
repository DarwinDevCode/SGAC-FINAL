package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademicoResponseDTO {
    private Integer idPeriodoAcademico;
    private String nombrePeriodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private Boolean activo;
}