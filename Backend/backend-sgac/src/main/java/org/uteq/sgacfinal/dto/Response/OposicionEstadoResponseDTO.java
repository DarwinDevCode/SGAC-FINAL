package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OposicionEstadoResponseDTO {
    private Integer idPostulacion;
    private String  temaSorteado;
    // Cuáles miembros ya guardaron su nota
    private Boolean decanoCalificado;
    private Boolean coordinadorCalificado;
    private Boolean docenteCalificado;
    // Promedio (solo visible cuando los 3 guardaron)
    private BigDecimal promedioOposicion;
    private Boolean todosCalificados;
}
