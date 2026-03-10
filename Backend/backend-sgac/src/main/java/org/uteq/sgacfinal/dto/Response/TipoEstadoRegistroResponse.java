package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TipoEstadoRegistroResponse {
    private Integer id;
    private String  nombreEstado;
    private String  descripcion;
}