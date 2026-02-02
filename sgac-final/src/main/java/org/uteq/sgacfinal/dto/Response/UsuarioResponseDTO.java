package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String cedula;
    private String correo;
    private String nombreUsuario;
    private LocalDate fechaCreacion;
    private Boolean activo;
}