package org.uteq.sgacfinal.dto.request.configuracion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AjusteCronogramaRequestDTO {
    @NotNull(message = "El ID del periodo es obligatorio")
    private Integer idPeriodo;

    @NotEmpty(message = "Debe enviar al menos una fase para el cronograma")
    @Valid
    private List<FaseCronogramaRequestDTO> fases;
}