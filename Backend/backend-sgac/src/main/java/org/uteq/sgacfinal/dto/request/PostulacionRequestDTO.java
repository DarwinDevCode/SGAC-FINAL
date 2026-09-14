package org.uteq.sgacfinal.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulacionRequestDTO {
    private Integer idConvocatoria;
    // El frontend (convocatorias.component.ts) envia esta clave JSON como
    // "idEstudiante" pero en realidad manda el id_usuario del estudiante
    // (ver PostulacionServiceImpl.registrarPostulacionCompleta, que busca
    // con estudianteRepository.findByUsuario_IdUsuario). Se mantiene el
    // nombre en el JSON por compatibilidad con el frontend ya desplegado,
    // pero el campo Java se llama por lo que realmente contiene.
    @JsonProperty("idEstudiante")
    private Integer idUsuario;
    private String observaciones;
    private Integer idTipoRequisito;
    private Integer idTipoEstado;
    private String observacionRequisito;
}