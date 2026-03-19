package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    private long postulantesSeleccionados;
    private long postulantesNoSeleccionados;
    private long postulantesEnEvaluacion;
    private long postulantesPendientes;

    @Builder.Default
    private List<ActividadCoordinadorDTO> actividadPorCoordinador = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActividadCoordinadorDTO {
        private String nombreCoordinador;
        private long totalConvocatorias;
    }
}
