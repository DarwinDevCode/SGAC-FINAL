package org.uteq.sgacfinal.dto.Request;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulacionRequestDTO {
    private Integer idConvocatoria;
    private Integer idEstudiante;
    private String observaciones;
    private Integer idTipoRequisito;
    private Integer idTipoEstado;
    private String observacionRequisito;
}