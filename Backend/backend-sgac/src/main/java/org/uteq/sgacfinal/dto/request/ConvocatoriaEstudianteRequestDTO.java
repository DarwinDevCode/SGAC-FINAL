package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConvocatoriaEstudianteRequestDTO {

    @NotNull(message = "El ID de usuario es requerido")
    private Integer idUsuario;
}

