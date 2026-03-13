package org.uteq.sgacfinal.dto.Response.convocatorias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvocatoriaComisionDTO {
    private Integer                   idConvocatoria;
    private String                    nombreAsignatura;
    private Integer                   idComision;
    private String                    nombreComision;
    private String                    fechaConformacion;
    private FaseEvaluacionDTO         faseEvaluacion;
    private List<PostulanteComisionDTO> postulantes;
}
