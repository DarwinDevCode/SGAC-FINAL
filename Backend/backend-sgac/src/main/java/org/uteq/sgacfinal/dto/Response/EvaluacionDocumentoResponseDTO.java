package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO para la respuesta de evaluación de documento
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionDocumentoResponseDTO {

    @JsonProperty("exito")
    private Boolean exito;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("id_requisito_adjunto")
    private Integer idRequisitoAdjunto;

    @JsonProperty("nuevo_estado")
    private String nuevoEstado;

    @JsonProperty("tiene_observados")
    private Boolean tieneObservados;

    @JsonProperty("todos_validados")
    private Boolean todosValidados;

    @JsonProperty("puede_aprobar_postulacion")
    private Boolean puedeAprobarPostulacion;

    /**
     * Fecha y hora límite para que el estudiante subsane el documento (si fue OBSERVADO).
     * Solo tiene valor cuando la acción fue OBSERVAR.
     */
    @JsonProperty("fecha_limite_subsanacion")
    private String fechaLimiteSubsanacion;
}

