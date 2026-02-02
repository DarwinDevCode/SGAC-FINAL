package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisitoAdjuntoRequestDTO {
    @NotNull
    private Integer idPostulacion;
    @NotNull
    private Integer idTipoRequisitoPostulacion;
    @NotNull
    private Integer idTipoEstadoRequisito;
    private byte[] archivo;
    private String nombreArchivo;
    private LocalDate fechaSubida;
    private String observacion;
}