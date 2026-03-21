package org.uteq.sgacfinal.dto.response.configuracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificarPostulantesResponseDTO {
    private boolean exito;
    private boolean tienePostulantes;
    private Integer totalPostulantes;
    private String  mensaje;
}