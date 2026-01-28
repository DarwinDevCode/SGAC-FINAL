package com.sgac.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequest {

    @NotBlank(message = "Los nombres son requeridos")
    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    private String apellidos;

    @NotBlank(message = "La cédula es requerida")
    @Size(max = 20, message = "La cédula no puede exceder 20 caracteres")
    private String cedula;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "El formato del correo no es válido")
    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    private String correo;

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres")
    private String contraseniaUsuario;

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;

    private List<Integer> rolesIds;
}
