package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecanoReporteCoordinadorDTO {
    private Integer idCoordinador;
    private String nombreCoordinador;
    private String carrera;
    private long convocatoriasCreadas;
    private long postulantesGestionados;
    private String estado;
}
