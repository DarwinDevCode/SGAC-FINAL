package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoObjetoResponseDTO {
    private Integer idTipoObjetoSeguridad;
    private String nombreTipoObjeto;
}