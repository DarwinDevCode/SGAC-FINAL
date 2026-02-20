package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroActividadRequestDTO {
    @NotNull
    private Integer idAyudantia;
    private String descripcionActividad;
    private String temaTratado;
    @NotNull
    private LocalDate fecha;
    private Integer numeroAsistentes;
    @NotNull
    private BigDecimal horasDedicadas;
    private String estadoRevision;
}