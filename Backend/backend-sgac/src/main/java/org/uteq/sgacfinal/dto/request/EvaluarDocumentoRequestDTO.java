package org.uteq.sgacfinal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para evaluar un documento individual de una postulación
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluarDocumentoRequestDTO {

    @NotNull(message = "El ID del requisito adjunto es obligatorio")
    @JsonProperty("id_requisito_adjunto")
    private Integer idRequisitoAdjunto;

    @NotBlank(message = "La acción es obligatoria")
    @JsonProperty("accion")
    private String accion; // VALIDAR, OBSERVAR, RECHAZAR

    @JsonProperty("observacion")
    private String observacion; // Requerido solo para OBSERVAR
}

