package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AyudantiaResponseDTO {
    private Integer idAyudantia;

    private Integer idTipoEstadoEvidenciaAyudantia;
    private String nombreEstadoEvidencia;
    private Integer idPostulacion;
    private String nombreEstudiante;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer horasCumplidas;
}