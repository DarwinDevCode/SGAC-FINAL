package org.uteq.sgacfinal.dto.response.configuracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificarFaseResponseDTO {
    private boolean valido;
    private String  mensaje;
    private Integer idPeriodo;
    private String  nombrePeriodo;
    private String  codigoFase;
    private String  nombreFase;
    private String  faseInicio;   // YYYY-MM-DD
    private String  faseFin;
}