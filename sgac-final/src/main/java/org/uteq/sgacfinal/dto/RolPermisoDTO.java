package org.uteq.sgacfinal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPermisoDTO {

    private String rol;
    private String objeto;
    private String permiso;
}
