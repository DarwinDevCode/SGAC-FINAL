package org.uteq.sgacfinal.dto.Request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TipoEstadoRequisitoRequestDTO {

    @NotBlank(message = "El nombre del estado es requerido")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombreEstado;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotBlank(message = "El código es requerido")
    @Size(max = 25, message = "El código no puede exceder 25 caracteres")
    private String codigo;
}