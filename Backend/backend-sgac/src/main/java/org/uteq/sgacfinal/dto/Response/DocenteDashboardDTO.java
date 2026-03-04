package org.uteq.sgacfinal.dto.Response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteDashboardDTO {

    private Integer totalConvocatoriasActivas;
    private Integer totalPostulacionesPendientes;
    private Integer totalAyudantesAsignados;
    private Integer totalActividadesPorRevisar;

    private List<UltimaActividadDTO> ultimasActividades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UltimaActividadDTO {
        private LocalDate fecha;
        private String nombreEstudiante;
        private String tema;
        private Integer idRegistro;
    }
}