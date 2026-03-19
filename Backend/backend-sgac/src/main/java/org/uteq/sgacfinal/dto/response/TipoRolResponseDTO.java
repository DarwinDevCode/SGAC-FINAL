package org.uteq.sgacfinal.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRolResponseDTO {
    private Integer idTipoRol;
    private String nombreTipoRol;
    private Boolean activo;
}