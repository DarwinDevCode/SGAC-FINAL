package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDocumentosDTO {

    private Integer pendientes;
    private Integer aprobados;
    private Integer observados;
    private Integer rechazados;
}