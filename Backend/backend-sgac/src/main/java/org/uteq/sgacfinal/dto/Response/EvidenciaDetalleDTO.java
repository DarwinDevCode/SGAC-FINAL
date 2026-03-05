package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaDetalleDTO {
    private Integer idEvidencia;
    private String nombreArchivo;
    private String tipoEvidencia;
    private String rutaArchivo;
}