package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraRequestDTO {

    @NotNull(message = "La facultad es obligatoria")
    private Integer idFacultad;

    @NotBlank(message = "El nombre de la carrera es obligatorio")
    private String nombreCarrera;
}