package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaRegistroActividadRequestDTO {
    @NotNull
    private Integer idRegistroActividad;
    private String tipoEvidencia;
    private byte[] archivo;
    private String nombreArchivo;
    private LocalDate fechaSubida;
    private Boolean activo;
}