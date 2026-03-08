package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para el detalle completo de una postulación vista por el coordinador
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePostulacionCoordinadorDTO {

    @JsonProperty("postulacion")
    private PostulacionInfoCoordinadorDTO postulacion;

    @JsonProperty("estudiante")
    private EstudianteInfoDTO estudiante;

    @JsonProperty("convocatoria")
    private ConvocatoriaInfoDTO convocatoria;

    @JsonProperty("documentos")
    private List<DocumentoEvaluacionDTO> documentos;

    @JsonProperty("resumen_documentos")
    private ResumenDocumentosEvaluacionDTO resumenDocumentos;

    @JsonProperty("puede_aprobar")
    private Boolean puedeAprobar;

    // Clases internas para los objetos anidados

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostulacionInfoCoordinadorDTO {
        @JsonProperty("id_postulacion")
        private Integer idPostulacion;

        @JsonProperty("fecha_postulacion")
        private LocalDate fechaPostulacion;

        @JsonProperty("estado_codigo")
        private String estadoCodigo;

        @JsonProperty("estado_nombre")
        private String estadoNombre;

        @JsonProperty("observaciones")
        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstudianteInfoDTO {
        @JsonProperty("id_estudiante")
        private Integer idEstudiante;

        @JsonProperty("nombre_completo")
        private String nombreCompleto;

        @JsonProperty("email")
        private String email;

        @JsonProperty("matricula")
        private String matricula;

        @JsonProperty("semestre")
        private Integer semestre;

        @JsonProperty("estado_academico")
        private String estadoAcademico;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConvocatoriaInfoDTO {
        @JsonProperty("id_convocatoria")
        private Integer idConvocatoria;

        @JsonProperty("asignatura")
        private String asignatura;

        @JsonProperty("docente")
        private String docente;

        @JsonProperty("fecha_publicacion")
        private LocalDate fechaPublicacion;

        @JsonProperty("fecha_cierre")
        private LocalDate fechaCierre;

        @JsonProperty("cupos_disponibles")
        private Integer cuposDisponibles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoEvaluacionDTO {
        @JsonProperty("id_requisito_adjunto")
        private Integer idRequisitoAdjunto;

        @JsonProperty("tipo_requisito")
        private String tipoRequisito;

        @JsonProperty("descripcion_requisito")
        private String descripcionRequisito;

        @JsonProperty("nombre_archivo")
        private String nombreArchivo;

        @JsonProperty("fecha_subida")
        private LocalDate fechaSubida;

        @JsonProperty("estado_codigo")
        private String estadoCodigo;

        @JsonProperty("id_tipo_estado_requisito")
        private Integer idTipoEstadoRequisito;

        @JsonProperty("observacion")
        private String observacion;

        @JsonProperty("tiene_archivo")
        private Boolean tieneArchivo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumenDocumentosEvaluacionDTO {
        @JsonProperty("total")
        private Integer total;

        @JsonProperty("pendientes")
        private Integer pendientes;

        @JsonProperty("aprobados")
        private Integer aprobados;

        @JsonProperty("observados")
        private Integer observados;

        @JsonProperty("rechazados")
        private Integer rechazados;
    }
}

