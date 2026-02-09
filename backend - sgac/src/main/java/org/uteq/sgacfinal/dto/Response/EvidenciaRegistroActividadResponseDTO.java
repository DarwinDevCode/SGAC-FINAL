package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaRegistroActividadResponseDTO {
    private Integer idEvidenciaRegistroActividad;
    private Integer idRegistroActividad;
    private String descripcionActividad;
    private String tipoEvidencia;
    private String nombreArchivo;
    private LocalDate fechaSubida;
    private Boolean activo;
}