package org.uteq.sgacfinal.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioTipoRolResponseDTO {
    private Integer idUsuario;
    private String nombreUsuario;
    private Integer idTipoRol;
    private String nombreRol;
    private Boolean activo;
}