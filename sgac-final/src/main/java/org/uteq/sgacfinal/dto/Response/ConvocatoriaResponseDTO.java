package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvocatoriaResponseDTO {
    private Integer idConvocatoria;
    private Integer idPeriodoAcademico;
    private String nombrePeriodo;
    private Integer idAsignatura;
    private String nombreAsignatura;
    private Integer idDocente;
    private String nombreDocente;
    private Integer cuposDisponibles;
    private LocalDate fechaPublicacion;
    private LocalDate fechaCierre;
    private String estado;
    private Boolean activo;
}