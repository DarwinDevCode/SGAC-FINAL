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
public class AuditoriaGlobalDTO {
    private Integer idLog;
    private LocalDateTime fecha;
    private String usuario;
    private String roles;
    private String facultad;
    private String carrera;
    private String accion;
    private String modulo;
    private String detalle;
}
