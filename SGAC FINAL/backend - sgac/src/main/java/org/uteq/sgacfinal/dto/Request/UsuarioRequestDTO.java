package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {
    @NotBlank
    private String nombres;
    @NotBlank
    private String apellidos;
    @NotBlank
    private String cedula;
    @NotBlank
    @Email
    private String correo;
    @NotBlank
    private String nombreUsuario;
    private String contraseniaUsuario;
    private Boolean activo;
}