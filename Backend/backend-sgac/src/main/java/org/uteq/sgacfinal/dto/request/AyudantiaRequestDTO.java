package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class
AyudantiaRequestDTO {

    @NotNull(message = "El estado de evidencia es obligatorio")
    private Integer idTipoEstadoEvidenciaAyudantia;

    @NotNull(message = "La postulación es obligatoria")
    private Integer idPostulacion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private java.math.BigDecimal horasCumplidas;
}