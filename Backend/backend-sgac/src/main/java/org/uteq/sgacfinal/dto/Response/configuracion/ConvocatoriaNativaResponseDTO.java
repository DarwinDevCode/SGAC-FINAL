package org.uteq.sgacfinal.dto.Response.configuracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvocatoriaNativaResponseDTO {
    private boolean exito;
    private String  mensaje;
    private Integer id;
}