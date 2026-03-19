package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoMasivoResponseDTO {
    private Integer totalProcesados;
    private Integer exitosos;
    private Integer fallidos;
    //private List<DetalleResultadoDTO> detalles;
    private Boolean exito;
    private String mensaje;
}
