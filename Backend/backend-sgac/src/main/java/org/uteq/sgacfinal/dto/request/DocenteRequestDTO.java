package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteRequestDTO {
    @NotNull
    private Integer idUsuario;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
}