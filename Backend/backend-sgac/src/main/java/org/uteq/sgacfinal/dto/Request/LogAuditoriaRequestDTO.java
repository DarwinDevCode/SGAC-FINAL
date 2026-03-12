package org.uteq.sgacfinal.dto.Request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAuditoriaRequestDTO {
    private Integer idUsuario;
    private String accion;
    private String tablaAfectada;
    private Integer registroAfectado;
    private String ipOrigen;
    private String valorAnterior;
    private String valorNuevo;
}
