package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AyudanteCatedraRequestDTO {

    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;

    private BigDecimal horasAyudante;
}