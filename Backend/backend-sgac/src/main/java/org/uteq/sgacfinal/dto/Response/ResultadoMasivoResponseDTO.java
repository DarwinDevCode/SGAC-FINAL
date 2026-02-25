package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
