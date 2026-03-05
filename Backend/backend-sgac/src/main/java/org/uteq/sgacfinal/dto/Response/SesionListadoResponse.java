package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class SesionListadoResponse {
    private Integer idRegistro;
    private LocalDate fecha;
    private String temaTratado;
    private String descripcion;
    private Integer numeroAsistentes;
    private BigDecimal horasDedicadas;
    private String estado;
    private Long totalEvidencias;
    private Boolean tieneObservacion;
}