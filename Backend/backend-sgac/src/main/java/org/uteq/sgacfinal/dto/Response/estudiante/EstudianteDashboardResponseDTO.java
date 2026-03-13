package org.uteq.sgacfinal.dto.Response.estudiante;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteDashboardResponseDTO {

    private Integer convocatoriasAbiertas;
    private Integer misPostulaciones;
    private String faseActual;
    private String periodoAcademico;

    private ResumenProcesoDTO resumenProceso;
    private UltimaPostulacionDTO ultimaPostulacion;
    private List<ConvocatoriaDestacadaDTO> convocatoriasDestacadas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumenProcesoDTO {
        private LocalDate fechaInicioPeriodo;
        private LocalDate fechaFinPeriodo;
        private LocalDate fechaInicioFaseActual;
        private LocalDate fechaFinFaseActual;
        private Integer porcentajeAvance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UltimaPostulacionDTO {
        private Integer idPostulacion;
        private String asignatura;
        private String docente;
        private LocalDate fechaPostulacion;
        private String estado;
        private String observacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConvocatoriaDestacadaDTO {
        private Integer idConvocatoria;
        private String nombreAsignatura;
        private Integer semestreAsignatura;
        private String nombreCarrera;
        private String nombreDocente;
        private Integer cuposDisponibles;
        private LocalDate fechaInicioPostulacion;
        private LocalDate fechaFinPostulacion;
        private String estadoConvocatoria;
        private Boolean puedePostular;
        private Boolean yaPostulado;
    }
}