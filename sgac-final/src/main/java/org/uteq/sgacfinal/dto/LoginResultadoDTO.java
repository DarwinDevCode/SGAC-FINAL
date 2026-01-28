package org.uteq.sgacfinal.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResultadoDTO {

    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String nombreUsuario;
    private List<String> roles;
    private List<RolPermisoDTO> permisos;
}