package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinadorPostulanteReporteDTO {
    private Integer idPostulacion;
    private String nombreEstudiante;
    private String cedula;
    private String nombreAsignatura;
    private String nombrePeriodo;
    private LocalDate fechaPostulacion;
    private String estadoEvaluacion;
}
