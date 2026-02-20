package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaResponseDTO {
    private Integer idAsignatura;
    private Integer idCarrera;
    private String nombreCarrera;
    private String nombreAsignatura;
    private Integer semestre;
    private Boolean activo;
}