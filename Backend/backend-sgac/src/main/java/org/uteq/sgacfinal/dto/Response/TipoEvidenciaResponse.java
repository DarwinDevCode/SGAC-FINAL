package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TipoEvidenciaResponse {
    private Integer id;
    private String  nombre;
    private String  extensionPermitida;
}