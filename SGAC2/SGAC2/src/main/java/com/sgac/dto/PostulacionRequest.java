package com.sgac.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulacionRequest {
    @NotNull(message = "El id de la convocatoria es requerido")
    private Integer idConvocatoria;

    @NotNull(message = "El id del estudiante es requerido")
    private Integer idEstudiante;

    private Integer idPlazoActividad;

    private LocalDate fechaPostulacion;

    @Size(max = 30, message = "El estado de postulación no puede exceder 30 caracteres")
    private String estadoPostulacion;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;
}
