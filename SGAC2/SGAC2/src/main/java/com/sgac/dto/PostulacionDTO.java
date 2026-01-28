package com.sgac.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulacionDTO {
    private Integer idPostulacion;
    private Integer idConvocatoria;
    private String nombreAsignatura;
    private Integer idEstudiante;
    private String nombreEstudiante;
    private LocalDate fechaPostulacion;
    private String estadoPostulacion;
    private String observaciones;
    private Boolean activo;
}
