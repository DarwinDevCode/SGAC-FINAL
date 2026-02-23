package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRequisitoPostulacionResponseDTO {
    private Integer idTipoRequisitoPostulacion;
    private String nombreRequisito;
    private String descripcion;
    private Boolean activo;
}