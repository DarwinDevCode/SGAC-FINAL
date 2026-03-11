package org.uteq.sgacfinal.dto.Response.configuracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;

@Data
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
