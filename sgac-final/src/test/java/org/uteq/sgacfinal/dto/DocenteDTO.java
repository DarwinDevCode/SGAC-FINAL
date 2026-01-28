package org.uteq.sgacfinal.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteDTO {
    private Integer idDocente;
    private Integer idUsuario;
    private String nombreCompleto;
    private String correo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
}
