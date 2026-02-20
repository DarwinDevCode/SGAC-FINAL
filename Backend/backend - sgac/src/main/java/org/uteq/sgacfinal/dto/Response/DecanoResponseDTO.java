package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecanoResponseDTO {
    private Integer idDecano;
    private Integer idUsuario;
    private String nombreCompletoUsuario;
    private Integer idFacultad;
    private String nombreFacultad;
    private LocalDate fechaInicioGestion;
    private LocalDate fechaFinGestion;
    private Boolean activo;
}