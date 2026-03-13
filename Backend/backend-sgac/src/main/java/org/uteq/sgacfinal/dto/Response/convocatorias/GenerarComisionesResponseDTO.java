package org.uteq.sgacfinal.dto.Response.convocatorias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerarComisionesResponseDTO {
    private boolean exito;
    private String  mensaje;
    private Integer comisionesCreadas;
    private Integer convocatoriasOmitidas;
}