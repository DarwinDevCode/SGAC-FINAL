package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminReporteGeneralDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private long totalRelacionado;
    private String estado;
    private String extraInfo;
}
