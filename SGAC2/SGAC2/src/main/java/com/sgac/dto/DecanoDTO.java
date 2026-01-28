package com.sgac.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecanoDTO {
    private Integer idDecano;
    private Integer idUsuario;
    private String nombreCompleto;
    private Integer idFacultad;
    private String nombreFacultad;
    private LocalDate fechaInicioGestion;
    private LocalDate fechaFinGestion;
    private Boolean activo;
}
