package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // @NotBlank no aplica a BigDecimal (Jakarta Validation exige CharSequence);
    // esto hacia que /api/auth/promover-estudiante fallara SIEMPRE con
    // "No validator could be found for constraint NotBlank... BigDecimal".
    @NotNull(message = "Las horas asignadas son obligatorias")
    @DecimalMin(value = "0.0", inclusive = false, message = "Las horas asignadas deben ser mayores a 0")
    private BigDecimal horasAsignadas;
}
