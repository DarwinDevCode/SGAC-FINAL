package org.uteq.sgacfinal.dto.Response;

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