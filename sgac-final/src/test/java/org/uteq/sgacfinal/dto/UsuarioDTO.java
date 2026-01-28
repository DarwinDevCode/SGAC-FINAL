package org.uteq.sgacfinal.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String cedula;
    private String correo;
    private String nombreUsuario;
    private LocalDate fechaCreacion;
    private Boolean activo;
    private List<TipoRolDTO> roles;
}
