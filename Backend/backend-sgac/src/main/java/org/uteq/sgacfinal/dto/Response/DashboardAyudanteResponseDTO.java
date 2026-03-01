package org.uteq.sgacfinal.dto.Response;

import java.util.List;

public class DashboardAyudanteResponseDTO {
    private String nombreAsignatura;
    private String nombreDocente;
    private String estadoAyudantia;

    private Double horasTotalesRequeridas;
    private Double horasAprobadas;
    private Double horasPendientes;
    private Double horasSemanalesActuales;

    private List<RegistroActividadResponseDTO> ultimasActividades;
    private Long notificacionesPendientes;

    private String periodoAcademico;

    private Long conteoObservaciones;
}