package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioTipoRolRequestDTO {
    @NotNull
    private Integer idUsuario;
    @NotNull
    private Integer idTipoRol;
    private Boolean activo;
}