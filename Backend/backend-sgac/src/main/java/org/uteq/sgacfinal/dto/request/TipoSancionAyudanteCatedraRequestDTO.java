package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TipoSancionAyudanteCatedraRequestDTO {

    @NotBlank(message = "El nombre del tipo de sanción es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreTipoSancion;

    @NotBlank(message = "El código es requerido")
    @Size(max = 25, message = "El código no puede exceder 25 caracteres")
    private String codigo;
}