package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisitoAdjuntoResponseDTO {
    private Integer idRequisitoAdjunto;
    private Integer idPostulacion;
    private Integer idTipoRequisitoPostulacion;
    private String nombreRequisito;
    private Integer idTipoEstadoRequisito;
    private String nombreEstado;
    private String nombreArchivo;
    private LocalDate fechaSubida;
    private String observacion;
}