package org.uteq.sgacfinal.dto.Request.configuracion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConvocatoriaActualizarRequestDTO {

    @NotNull(message = "idConvocatoria es obligatorio")
    private Integer idConvocatoria;

    @NotNull(message = "tipoEdicion es obligatorio")
    @Pattern(regexp = "PARCIAL|COMPLETA",
            message = "tipoEdicion debe ser PARCIAL o COMPLETA")
    private String tipoEdicion;

    @Min(value = 1, message = "cuposDisponibles debe ser ≥ 1")
    private Integer cuposDisponibles;

    private String  estado;

    /** Solo se aplica en tipoEdicion = COMPLETA */
    private Integer idDocente;

    /** Solo se aplica en tipoEdicion = COMPLETA */
    private Integer idAsignatura;
}