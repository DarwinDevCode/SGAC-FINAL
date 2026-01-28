package org.uteq.sgacfinal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRolDTO {
    private Integer idTipoRol;
    private String nombreTipoRol;
    private Boolean activo;
}
