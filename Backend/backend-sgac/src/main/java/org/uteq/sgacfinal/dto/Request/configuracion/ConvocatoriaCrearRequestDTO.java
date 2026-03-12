package org.uteq.sgacfinal.dto.Request.configuracion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConvocatoriaCrearRequestDTO {

    @NotNull(message = "idAsignatura es obligatorio")
    private Integer idAsignatura;

    @NotNull(message = "idDocente es obligatorio")
    private Integer idDocente;

    @NotNull(message = "cuposDisponibles es obligatorio")
    @Min(value = 1, message = "cuposDisponibles debe ser ≥ 1")
    private Integer cuposDisponibles;

    private String estado = "ABIERTA";
}