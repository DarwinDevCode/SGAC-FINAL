package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingEvaluacionDTO {
    private Integer idPostulacion;
    private String  nombreEstudiante;
    private String  matricula;
    private BigDecimal totalMeritos;
    private BigDecimal promedioOposicion;
    private BigDecimal totalFinal;
    private BigDecimal matSum;         // suma criterio_material de los 3 evaluadores (desempate)
    private BigDecimal pertinenciaSum; // suma criterio_pertinencia de los 3 (desempate)
    private String  estado;            // GANADOR, APTO, NO_APTO, DESIERTO
    private Long    posicion;
    private Boolean empate;            // flag para que el frontend resalte la fila
}
