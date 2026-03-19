package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecanoEstadisticasDTO {
    private long totalConvocatorias;
    private long convocatoriasActivas;
    private long convocatoriasInactivas;
    private long totalPostulantes;
    private long postulantesAprobados;
    private long postulantesRechazados;
    private long postulantesEnEvaluacion;
    private List<ActividadCoordinadorDTO> actividadPorCoordinador;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActividadCoordinadorDTO {
        private String nombreCoordinador;
        private long totalConvocatorias;
    }
}
