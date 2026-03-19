package org.uteq.sgacfinal.dto.response.estudiante;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO que mapea el resultado de la función:
 * convocatoria.fn_listar_convocatorias_estudiante(p_id_usuario)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConvocatoriaEstudianteDTO {
    private Integer idConvocatoria;
    private String nombreAsignatura;
    private Integer semestreAsignatura;
    private String nombreCarrera;
    private String nombreDocente;
    private Integer cuposDisponibles;
    private LocalDate fechaInicioPostulacion;
    private LocalDate fechaFinPostulacion;
    private String estadoConvocatoria;
    private Boolean puedePostular;
}
