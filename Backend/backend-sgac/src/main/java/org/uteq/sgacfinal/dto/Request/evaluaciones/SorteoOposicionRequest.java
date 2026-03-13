package org.uteq.sgacfinal.dto.Request.evaluaciones;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SorteoOposicionRequest {

    @NotNull(message = "El id de convocatoria es requerido")
    private Integer idConvocatoria;

    @NotNull(message = "La fecha de evaluación es requerida")
    private String fecha;

    @NotNull(message = "La hora de inicio es requerida")
    private String horaInicio;

    @NotNull(message = "El lugar es requerido")
    private String lugar;
}