package org.uteq.sgacfinal.dto.response.convocatorias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiembroComisionDTO {
    private Integer idUsuario;
    private String  nombres;
    private String  apellidos;
    private String  cargo;
    private String  correo;
}