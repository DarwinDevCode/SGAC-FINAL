package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroActividadResumenDTO {
    private Integer idRegistroActividad;
    private LocalDate fecha;
    private String temaTratado;
    private BigDecimal horasDedicadas;
    private String estadoRevision;
    private Integer totalEvidencias;
}
