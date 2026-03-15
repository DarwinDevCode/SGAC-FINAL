package org.uteq.sgacfinal.dto.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvaluacionWsMessage {
    private String tipo;
    private Integer idConvocatoria;
    private Integer idEvaluacionOposicion;
    private String  nuevoEstado;
    private String  nombreEstado;
    private String  horaInicioReal;
    private String  serverTimestamp;
    private String  horaFinReal;
    private Double  puntajeFinal;
    private Integer idUsuario;
    private Boolean todosFinalizaron;
    private String  mensaje;
    @Builder.Default
    private String  timestamp = Instant.now().toString();
}