package org.uteq.sgacfinal.dto.Response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminConsultaDTO {
    
    // KPIs
    private long totalUsuarios;
    private long totalPostulaciones;
    private long totalConvocatorias;
    private String periodoActivo;
    
    // Temporal stats (Line Chart)
    private List<EstadisticaMensualDTO> estadisticasMensuales;
    
    // Role distribution (Pie Chart)
    private List<RolEstadisticaDTO> distribucionRoles;
    
    // Faculty distribution (Doughnut/Bar Chart)
    private List<FacultadEstadisticaDTO> distribucionFacultades;
    
    // Recent activity
    private List<LogActividadDTO> ultimasAcciones;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EstadisticaMensualDTO {
        private String mes;
        private long postulaciones;
        private long convocatorias;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RolEstadisticaDTO {
        private String rol;
        private long cantidad;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FacultadEstadisticaDTO {
        private String facultad;
        private long cantidadPostulaciones;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogActividadDTO {
        private Integer idLog;
        private String usuario;
        private String accion;
        private String modulo;
        private String fecha;
    }
}
