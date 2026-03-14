package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SincronizarCargaRequest {

    @NotNull(message = "El ID del docente es obligatorio")
    private Integer idDocente;

    @NotNull(message = "La lista de asignaturas es obligatoria (puede ser vacía)")
    private List<Integer> asignaturasIds;
}