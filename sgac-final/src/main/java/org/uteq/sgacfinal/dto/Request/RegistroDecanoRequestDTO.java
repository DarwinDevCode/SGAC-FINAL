package org.uteq.sgacfinal.dto.Request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDecanoRequestDTO {
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
    @NotBlank
    private String contrasenia;
    @NotNull
    private Integer idFacultad;
    @NotNull
    private LocalDate fechaInicioGestion;
    @NotNull
    private LocalDate fechaFinGestion;
}