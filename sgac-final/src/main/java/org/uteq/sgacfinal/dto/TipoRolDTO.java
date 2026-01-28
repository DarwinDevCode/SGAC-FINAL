package org.uteq.sgacfinal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRolDTO {

    private Integer idTipoRol;
    private String nombreTipoRol;
    private Boolean activo;
}
