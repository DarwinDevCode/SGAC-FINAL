package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;
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
    private List<String> roles;
    private String token;
    private Boolean activo;
}