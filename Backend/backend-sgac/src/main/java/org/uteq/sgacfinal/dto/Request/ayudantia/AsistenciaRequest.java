package org.uteq.sgacfinal.dto.Request.ayudantia;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsistenciaRequest {

    @NotNull(message = "El ID del participante es obligatorio.")
    private Integer idParticipanteAyudantia;

    @NotNull(message = "Debe indicar si el participante asistió o no.")
    private Boolean asistio;
}