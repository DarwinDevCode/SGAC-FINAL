package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para evaluar una evidencia (evidencia_registro_actividad).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluarEvidenciaRequestDTO {

    /**
     * Estados: 1(SUBIDO),2(REVISADO),3(APROBADO),4(RECHAZADO),5(OBSERVADO)
     */
    @NotNull
    private Integer idTipoEstadoEvidencia;

    @Size(max = 500)
    private String observaciones;
}

