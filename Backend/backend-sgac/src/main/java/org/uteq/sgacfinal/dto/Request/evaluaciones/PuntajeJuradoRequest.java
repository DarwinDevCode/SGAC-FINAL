package org.uteq.sgacfinal.dto.Request.evaluaciones;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PuntajeJuradoRequest {

    @NotNull(message = "El id de evaluación es requerido")
    private Integer idEvaluacionOposicion;

    @NotNull(message = "El id de usuario es requerido")
    private Integer idUsuario;

    @NotNull @DecimalMin("0.0") @DecimalMax("10.0")
    private BigDecimal puntajeMaterial;

    @NotNull @DecimalMin("0.0") @DecimalMax("4.0")
    private BigDecimal puntajeExposicion;

    @NotNull @DecimalMin("0.0") @DecimalMax("6.0")
    private BigDecimal puntajeRespuestas;

    private boolean finalizar = false;

    private Integer idConvocatoria;
}