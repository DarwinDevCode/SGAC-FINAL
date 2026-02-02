package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulacionRequestDTO {
    @NotNull
    private Integer idConvocatoria;
    @NotNull
    private Integer idEstudiante;
    private Integer idPlazoActividad;
    private LocalDate fechaPostulacion;
    private String estadoPostulacion;
    private String observaciones;
    private Boolean activo;
}