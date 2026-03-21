package org.uteq.sgacfinal.dto.response.convocatorias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostulanteComisionDTO {
    private Integer idPostulacion;
    private String  nombres;
    private String  apellidos;
    private String  correo;
    private String  fechaPostulacion;
    private String  estadoPostulacion;
    private String  codigoEstado;
}

