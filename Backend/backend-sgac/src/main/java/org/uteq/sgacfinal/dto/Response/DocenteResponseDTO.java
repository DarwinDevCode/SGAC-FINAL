package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteResponseDTO {
    private Integer idDocente;
    private Integer idUsuario;
    private String nombreCompletoUsuario;
    private String correoUsuario;
    private String cedulaUsuario;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
}