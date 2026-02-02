package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComisionSeleccionRequestDTO {
    @NotNull
    private Integer idConvocatoria;
    @NotBlank
    private String nombreComision;
    private LocalDate fechaConformacion;
    private Boolean activo;
}