package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalificacionOposicionResponseDTO {
    private Integer idCalificacion;
    private Integer idPostulacion;
    private Integer idEvaluador;
    private String  rolEvaluador;
    private BigDecimal criterioMaterial;
    private BigDecimal criterioCalidad;
    private BigDecimal criterioPertinencia;
    private BigDecimal subtotal;
    private LocalDateTime fechaRegistro;
}
