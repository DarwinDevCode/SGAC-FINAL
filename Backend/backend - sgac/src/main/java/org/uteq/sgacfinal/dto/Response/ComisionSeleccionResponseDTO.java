package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComisionSeleccionResponseDTO {
    private Integer idComisionSeleccion;
    private Integer idConvocatoria;
    private String nombreComision;
    private LocalDate fechaConformacion;
    private Boolean activo;
}