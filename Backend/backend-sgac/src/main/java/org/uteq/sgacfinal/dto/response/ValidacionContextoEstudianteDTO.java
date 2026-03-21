package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidacionContextoEstudianteDTO {
    private Integer idEstudiante;
    private Boolean esValido;
    private String mensaje;
}

