package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAuditoriaRequestDTO {
    @NotNull
    private Integer idUsuario;
    private String accion;
    private String tablaAfectada;
    private Integer registroAfectado;
    private String ipOrigen;
    private String valorAnterior;
    private String valorNuevo;
}