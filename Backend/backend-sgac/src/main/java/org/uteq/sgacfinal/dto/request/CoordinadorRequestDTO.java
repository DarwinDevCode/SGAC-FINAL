package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinadorRequestDTO {
    @NotNull
    private Integer idUsuario;
    @NotNull
    private Integer idCarrera;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
}