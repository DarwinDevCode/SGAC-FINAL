package org.uteq.sgacfinal.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermisoDTO {
    private String rol;
    private String objeto;
    private String permiso;
}
