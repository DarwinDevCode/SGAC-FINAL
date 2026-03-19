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
public class CoordinadorEstadisticasDTO {

    private Long totalConvocatoriasPropias;
    private Long convocatoriasActivas;
    private Long convocatoriasInactivas;

    private Long totalPostulantesRecibidos;
    private Long postulantesAprobados;
    private Long postulantesRechazados;
    private Long postulantesEnEvaluacion;
    private Long postulantesPendientes;

    // Lista para gráfico top 5 convocatorias por postulantes
    @Builder.Default
    private List<PostulantesPorConvocatoriaDTO> postulantesPorConvocatoria = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostulantesPorConvocatoriaDTO {
        private String tituloConvocatoria;
        private Long cantidadPostulantes;
    }
}
