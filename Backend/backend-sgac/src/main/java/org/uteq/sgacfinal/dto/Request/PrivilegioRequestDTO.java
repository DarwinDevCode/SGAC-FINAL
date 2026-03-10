package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivilegioRequestDTO {

    @NotBlank(message = "El nombre del privilegio es requerido")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombrePrivilegio;

    @Size(max = 1, message = "El código interno debe ser de 1 carácter")
    private String codigoInterno;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
}

