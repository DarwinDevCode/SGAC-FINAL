package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecanoRequestDTO {
    @NotNull
    private Integer idUsuario;
    @NotNull
    private Integer idFacultad;
    @NotNull
    private LocalDate fechaInicioGestion;
    private LocalDate fechaFinGestion;
    private Boolean activo;
}