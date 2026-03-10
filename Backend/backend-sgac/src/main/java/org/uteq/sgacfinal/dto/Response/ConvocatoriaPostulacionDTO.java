package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("estado_admin")
    private String estadoAdmin;
}