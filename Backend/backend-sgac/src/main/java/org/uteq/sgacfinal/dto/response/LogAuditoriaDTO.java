package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogAuditoriaDTO {
    private Integer idLog;
    private String nombreUsuario;
    private String accion;
    private String tablaAfectada;
    private LocalDateTime fechaHora;
}
