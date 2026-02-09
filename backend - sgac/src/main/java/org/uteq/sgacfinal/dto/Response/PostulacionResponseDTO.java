package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulacionResponseDTO {
    private Integer idPostulacion;
    private Integer idConvocatoria;
    private String asignaturaConvocatoria;
    private Integer idEstudiante;
    private String nombreCompletoEstudiante;
    private String matriculaEstudiante;
    private Integer idPlazoActividad;
    private LocalDate fechaPostulacion;
    private String estadoPostulacion;
    private String observaciones;
    private Boolean activo;
}