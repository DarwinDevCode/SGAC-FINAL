package org.uteq.sgacfinal.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TipoFaseRequestDTO {

    @NotBlank(message = "El código es requerido")
    @Size(max = 25, message = "El código no puede exceder 25 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre de la fase es requerido")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El orden es requerido")
    @Min(value = 1, message = "El orden debe ser mayor a 0")
    private Integer orden;
}

