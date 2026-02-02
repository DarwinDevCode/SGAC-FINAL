package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSancionAyudanteCatedraResponseDTO {
    private Integer idTipoSancionAyudanteCatedra;
    private String nombreTipoSancion;
    private Boolean activo;
}