package org.uteq.sgacfinal.dto.response.convocatorias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalizarSeleccionResponseDTO {
    private boolean exito;
    private String  mensaje;
    private int     seleccionados;
    private int     elegibles;
    private int     noSeleccionados;
}