package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolResumenResponseDTO {
    private Integer idTipoRol;
    private String nombreLogico;
    private String nombreRolBd;
    private Boolean activo;
}