package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinadorResponseDTO {
    private Integer idCoordinador;
    private Integer idUsuario;
    private String nombreCompletoUsuario;
    private String cedula;
    private Integer idCarrera;
    private String nombreCarrera;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
}