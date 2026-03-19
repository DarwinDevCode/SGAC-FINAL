package org.uteq.sgacfinal.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String nombreUsuario;
    private String rolActual;
    private List<TipoRolResponseDTO> roles;
    private String token;
    private String cedula;
    private Boolean activo;
}