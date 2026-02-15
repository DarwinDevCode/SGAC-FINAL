package org.uteq.sgacfinal.dto.Response;

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