package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO para el listado de postulaciones vistas por el coordinador
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulacionListadoCoordinadorDTO {

    @JsonProperty("id_postulacion")
    private Integer idPostulacion;

    @JsonProperty("id_convocatoria")
    private Integer idConvocatoria;

    @JsonProperty("id_estudiante")
    private Integer idEstudiante;

    @JsonProperty("nombre_estudiante")
    private String nombreEstudiante;

    @JsonProperty("matricula")
    private String matricula;

    @JsonProperty("semestre")
    private Integer semestre;

    @JsonProperty("nombre_asignatura")
    private String nombreAsignatura;

    @JsonProperty("nombre_carrera")
    private String nombreCarrera;

    @JsonProperty("fecha_postulacion")
    private LocalDate fechaPostulacion;

    @JsonProperty("estado_codigo")
    private String estadoCodigo;

    @JsonProperty("estado_nombre")
    private String estadoNombre;

    @JsonProperty("requiere_atencion")
    private Boolean requiereAtencion;

    @JsonProperty("total_documentos")
    private Long totalDocumentos;

    @JsonProperty("documentos_pendientes")
    private Long documentosPendientes;

    @JsonProperty("documentos_aprobados")
    private Long documentosAprobados;

    @JsonProperty("documentos_observados")
    private Long documentosObservados;

    @JsonProperty("observaciones")
    private String observaciones;
}

