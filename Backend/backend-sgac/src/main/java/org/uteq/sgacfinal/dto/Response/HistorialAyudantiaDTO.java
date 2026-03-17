package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialAyudantiaDTO {
    private Integer idAyudantia;
    private Integer idPostulacion;
    private String nombreEstudiante;
    private String cedula;
    // Asignatura
    private String nombreAsignatura;
    private String codigoAsignatura;
    // Período académico
    private String nombrePeriodo;
    private LocalDate inicioPeriodo;
    private LocalDate finPeriodo;
    // Datos de la ayudantía
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal horasCumplidas;
    private Integer totalSesiones;
    private String resultadoFinal;   // APROBADO / REPROBADO / EN_CURSO
    private String estadoAyudantia;
}
