package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SancionAyudanteCatedraResponseDTO {
    private Integer idSancionAyudanteCatedra;
    private Integer idTipoSancionAyudanteCatedra;
    private String nombreTipoSancion;
    private Integer idAyudanteCatedra;
    private String nombreCompletoAyudante;
    private LocalDate fechaSancion;
    private String motivo;
    private Boolean activo;
}