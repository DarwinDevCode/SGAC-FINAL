package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvocatoriaPostulacionDTO {

    @JsonProperty("id_convocatoria")
    private Integer idConvocatoria;

    @JsonProperty("nombre_asignatura")
    private String nombreAsignatura;

    @JsonProperty("semestre_asignatura")
    private Integer semestreAsignatura;

    @JsonProperty("nombre_carrera")
    private String nombreCarrera;

    @JsonProperty("nombre_docente")
    private String nombreDocente;

    @JsonProperty("cupos_disponibles")
    private Integer cuposDisponibles;

    @JsonProperty("estado_convocatoria")
    private String estadoConvocatoria;

    @JsonProperty("fecha_publicacion")
    private LocalDate fechaPublicacion;

    @JsonProperty("fecha_cierre")
    private LocalDate fechaCierre;
}