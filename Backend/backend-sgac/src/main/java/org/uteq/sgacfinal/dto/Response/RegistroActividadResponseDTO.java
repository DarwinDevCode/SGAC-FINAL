package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroActividadResponseDTO {
    private Integer idRegistroActividad;
    private Integer idAyudantia;
    private String descripcionActividad;
    private String temaTratado;
    private LocalDate fecha;
    private Integer numeroAsistentes;
    private BigDecimal horasDedicadas;
    private String estadoRevision;
}