package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroActividadDetalleDTO {
    private Integer idRegistroActividad;
    private String descripcionActividad;
    private String temaTratado;
    private LocalDate fecha;
    private BigDecimal horasDedicadas;
    private String estadoRevision;
    private List<EvidenciaDetalleDTO> evidencias;
}