package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class SesionDetalleResponse {
    private Integer idRegistro;
    private LocalDate fecha;
    private String temaTratado;
    private String descripcion;
    private Integer numeroAsistentes;
    private BigDecimal horasDedicadas;
    private String estado;
    private String nombreAsignatura;
    private String nombreDocente;
    private String nombrePeriodo;
    private List<EvidenciaResponse> evidencias;
}