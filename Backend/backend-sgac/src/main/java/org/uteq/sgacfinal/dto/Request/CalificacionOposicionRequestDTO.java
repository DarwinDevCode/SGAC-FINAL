package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalificacionOposicionRequestDTO {

    @NotNull(message = "El ID de postulación es obligatorio")
    private Integer idPostulacion;

    @NotNull(message = "El ID del evaluador es obligatorio")
    private Integer idEvaluador;

    @NotBlank(message = "El rol del evaluador es obligatorio")
    private String rolEvaluador; // DECANO, COORDINADOR, DOCENTE

    @NotNull
    @DecimalMin("0.00") @DecimalMax("10.00")
    private BigDecimal criterioMaterial;

    @NotNull
    @DecimalMin("0.00") @DecimalMax("4.00")
    private BigDecimal criterioCalidad;

    @NotNull
    @DecimalMin("0.00") @DecimalMax("6.00")
    private BigDecimal criterioPertinencia;
}
