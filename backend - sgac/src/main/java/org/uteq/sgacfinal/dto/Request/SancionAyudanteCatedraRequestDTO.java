package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SancionAyudanteCatedraRequestDTO {
    @NotNull
    private Integer idTipoSancionAyudanteCatedra;
    @NotNull
    private Integer idAyudanteCatedra;
    private LocalDate fechaSancion;
    private String motivo;
    private Boolean activo;
}