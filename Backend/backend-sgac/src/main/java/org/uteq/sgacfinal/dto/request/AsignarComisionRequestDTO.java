package org.uteq.sgacfinal.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarComisionRequestDTO {
    private Integer idPostulacion;
    private Integer idComisionSeleccion;
    private String temaExposicion;
    private LocalDate fechaEvaluacion;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String lugar;
}
