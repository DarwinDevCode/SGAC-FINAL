package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Setter
@Getter
public class PromoverEstudianteAyudanteRequest {
    @NotBlank
    private String username;
    @NotBlank
    private BigDecimal horasAsignadas;
}
