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
public class CoordinadorEstadisticasDTO {
    
    private Long totalConvocatoriasPropias;
    private Long convocatoriasActivas;
    private Long convocatoriasInactivas;
    
    private Long totalPostulantesRecibidos;
    private Long postulantesAprobados;
    private Long postulantesRechazados;
    private Long postulantesEnEvaluacion;
    
    // Lista para gráfico de top 5 o total de postulantes por convocatoria específica
    private List<PostulantesPorConvocatoriaDTO> postulantesPorConvocatoria;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostulantesPorConvocatoriaDTO {
        private String tituloConvocatoria;
        private Long cantidadPostulantes;
    }
}
