package org.uteq.sgacfinal.dto.Request.estudiante;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListarConvocatoriaRequest {

    @NotNull(message = "El ID de usuario es requerido")
    private Integer idUsuario;
}
