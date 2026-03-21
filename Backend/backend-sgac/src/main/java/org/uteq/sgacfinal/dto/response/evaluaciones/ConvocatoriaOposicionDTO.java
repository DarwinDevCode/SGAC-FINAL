package org.uteq.sgacfinal.dto.response.evaluaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvocatoriaOposicionDTO {
    private Integer idConvocatoria;
    private String nombreAsignatura;
    private Integer semestreAsignatura;
    private String nombreCarrera;
    private String nombreFacultad;
    private String nombreDocente;
    private Integer cuposDisponibles;
    private String estadoConvocatoria;
    private Integer totalPostulantesAptos;
    private Boolean tieneComision;
    private Boolean tieneSorteo;
}