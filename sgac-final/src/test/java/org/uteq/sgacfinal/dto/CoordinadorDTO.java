package org.uteq.sgacfinal.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinadorDTO {
    private Integer idCoordinador;
    private Integer idUsuario;
    private String nombreCompleto;
    private Integer idCarrera;
    private String nombreCarrera;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
}
