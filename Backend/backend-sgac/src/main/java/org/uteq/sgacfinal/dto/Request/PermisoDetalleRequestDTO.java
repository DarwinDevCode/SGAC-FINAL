package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermisoDetalleRequestDTO {
    @NotBlank(message = "El privilegio es obligatorio")
    private String privilegio;

    @NotNull(message = "El estado de otorgar es obligatorio")
    private Boolean otorgar;
}