package org.uteq.sgacfinal.dto.response;

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