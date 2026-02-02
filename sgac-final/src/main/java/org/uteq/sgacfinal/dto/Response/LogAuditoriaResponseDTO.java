package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAuditoriaResponseDTO {
    private Integer idLogAuditoria;
    private Integer idUsuario;
    private String nombreUsuario;
    private String accion;
    private String tablaAfectada;
    private Integer registroAfectado;
    private LocalDateTime fechaHora;
    private String ipOrigen;
    private String valorAnterior;
    private String valorNuevo;
}