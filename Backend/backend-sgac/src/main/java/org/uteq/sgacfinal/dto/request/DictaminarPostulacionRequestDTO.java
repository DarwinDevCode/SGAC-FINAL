package org.uteq.sgacfinal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para dictaminar (aprobar/rechazar) una postulación completa
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictaminarPostulacionRequestDTO {

    @NotNull(message = "El ID de la postulación es obligatorio")
    @JsonProperty("id_postulacion")
    private Integer idPostulacion;

    @NotBlank(message = "La acción es obligatoria")
    @JsonProperty("accion")
    private String accion; // APROBAR, RECHAZAR

    @JsonProperty("observacion")
    private String observacion; // Requerido solo para RECHAZAR
}

