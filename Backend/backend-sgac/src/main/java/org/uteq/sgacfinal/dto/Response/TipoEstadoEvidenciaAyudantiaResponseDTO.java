package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadoEvidenciaAyudantiaResponseDTO {
    private Integer idTipoEstadoEvidenciaAyudantia;
    private String nombreEstado;
    private String descripcion;
    private Boolean activo;
}