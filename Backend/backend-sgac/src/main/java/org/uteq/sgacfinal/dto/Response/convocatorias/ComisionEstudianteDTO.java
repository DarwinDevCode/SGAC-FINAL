package org.uteq.sgacfinal.dto.Response.convocatorias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComisionEstudianteDTO {
    private Integer               idComision;
    private Integer               idConvocatoria;
    private String                nombreAsignatura;
    private String                nombreComision;
    private String                fechaConformacion;
    private List<MiembroComisionDTO> miembros;
}