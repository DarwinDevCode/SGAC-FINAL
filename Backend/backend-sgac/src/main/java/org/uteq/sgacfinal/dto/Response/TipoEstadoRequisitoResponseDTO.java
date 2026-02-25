package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstadoRequisitoResponseDTO {
    private Integer idTipoEstadoRequisito;
    private String nombreEstado;
    private String descripcion;
    private Boolean activo;
}