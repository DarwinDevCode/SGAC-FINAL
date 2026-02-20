package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAyudanteCatedraRequestDTO {
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
    private String username;
    @NotBlank
    private String password;
    @NotNull
    private BigDecimal horasAyudante;
}


