package org.uteq.sgacfinal.dto.Request.evaluaciones;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CambiarEstadoEvaluacionRequest {

    @NotNull(message = "El id de evaluación es requerido")
    private Integer idEvaluacionOposicion;

    @NotNull(message = "La acción es requerida")
    private String accion;

    private Integer idConvocatoria;
}