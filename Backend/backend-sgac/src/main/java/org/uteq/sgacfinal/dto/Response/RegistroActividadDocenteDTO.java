package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RegistroActividadDocenteDTO {
    private Integer idRegistroActividad;
    private Integer idAyudantia;
    private String descripcionActividad;
    private String temaTratado;
    private LocalDate fecha;
    private Integer numeroAsistentes;
    private BigDecimal horasDedicadas;
    private String estadoRevision;
    private String observaciones;
    private LocalDate fechaObservacion;
    private List<EvidenciaDocenteDTO> evidencias;
}
