package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para evaluar una actividad (registro_actividad).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluarActividadRequestDTO {

    /**
     * Estados: 1(PENDIENTE), 2(APROBADO), 3(OBSERVADO), 4(RECHAZADO)
     */
    @NotNull
    private Integer idTipoEstadoRegistro;

    @Size(max = 500)
    private String observaciones;
}

