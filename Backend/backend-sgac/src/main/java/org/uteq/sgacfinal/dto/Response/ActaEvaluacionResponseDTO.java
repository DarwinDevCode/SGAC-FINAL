package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActaEvaluacionResponseDTO {
    private Integer idActa;
    private Integer idPostulacion;
    private String  tipoActa;
    private String  urlDocumento;
    private LocalDateTime fechaGeneracion;
    private Boolean confirmadoDecano;
    private Boolean confirmadoCoordinador;
    private Boolean confirmadoDocente;
    private String  estado;
}
