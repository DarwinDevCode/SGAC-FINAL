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
public class ConvocatoriaEstudianteResponseDTO {
    private Integer idConvocatoria;
    private String nombreAsignatura;
    private Integer semestreAsignatura;
    private String nombreCarrera;
    private String nombreDocente;
    private Integer cuposDisponibles;
    private LocalDate fechaPublicacion;
    private LocalDate fechaCierre;
    private String estado;
}
