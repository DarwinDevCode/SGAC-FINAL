package org.uteq.sgacfinal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ObservacionWsDTO {
    private String tipo;             // ACTIVIDAD | EVIDENCIA
    private Integer idReferencia;
    private String nombreActividad;
    private String observacion;
    private String estadoNuevo;
    private LocalDate fecha;
    private String nombreDocente;
}
